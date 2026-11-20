#!/usr/bin/env python3
"""Stage only the 21 real PDFs exercised by the smoke integration test on an explicit AVD."""
import argparse
from pathlib import Path
import capture_store_screenshots as c
from sample_pdf_inputs import resolve_inputs


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--device',choices=c.DEVICES,required=True)
    parser.add_argument('--sample-pdf-dir',type=Path)
    args=parser.parse_args();root=Path(__file__).resolve().parents[1]
    ids=[f'ep{episode}_{lang}' for lang in ['es','it','pt','vi','fil','ne','id'] for episode in [1,2,3]]
    pdfs=resolve_inputs(root,ids,args.sample_pdf_dir)  # Fail before any device operation.
    out=root/'.artifacts/sample-validation';out.mkdir(parents=True,exist_ok=True)
    apks=[root/'app/build/outputs/apk/debug/app-debug.apk',root/'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk']
    if not all(p.is_file() for p in apks):raise RuntimeError('Build assembleDebug assembleDebugAndroidTest before integration verification')
    c.WORK.mkdir(parents=True,exist_ok=True);device=c.Device(args.device)
    try:
        device.start()
        for apk in apks:device.adb('install','-r','-t',apk,timeout=180)
        device.adb('shell','run-as',c.PACKAGE,'mkdir','-p','files/sample_smoke')
        for source in pdfs.values():
            staged='/data/local/tmp/sample-smoke.pdf';target='files/sample_smoke/'+source.name
            device.adb('push',source,staged);device.adb('shell','run-as',c.PACKAGE,'cp',staged,target)
            if device.text('shell','run-as',c.PACKAGE,'sha256sum',target).split()[0]!=c.sha(source):raise RuntimeError('Device fixture checksum mismatch')
        result=device.adb('shell','am','instrument','-w','-r','-e','class','com.github.tyamada.mihirakipdfviewer_android.SamplePdfSmokeTest','-e','sampleSmoke','true',c.RUNNER,check=False,timeout=1800)
        log=result.stdout.decode(errors='replace')+'\n'+result.stderr.decode(errors='replace');(out/'instrumentation.log').write_text(log)
        if 'OK (3 tests)' not in log or 'FAILURES!!!' in log:raise RuntimeError('Sample smoke integration failed; see instrumentation.log')
        print('PASS: 3 integration tests; 21 real PDFs staged and verified')
    finally:device.stop()

if __name__=='__main__':main()
