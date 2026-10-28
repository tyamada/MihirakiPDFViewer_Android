#!/usr/bin/env python3
"""
YouTube Video Upload Helper Script (YouTube Data API v3)
Requirements:
    pip install google-auth google-auth-oauthlib google-api-python-client

Usage:
    python3 scripts/upload_to_youtube.py --file shorts_output/mihiraki_shorts_en.mp4 --title "Mihiraki PDF Viewer - Perfect Right-to-Left & Two-Page Spread Reader" --description "Download Mihiraki PDF Viewer on Google Play: https://play.google.com/store/apps/details?id=com.github.tyamada.MihirakiPDFViewer_Android #shorts #pdfviewer"
"""

import argparse
import os
import sys

def main():
    parser = argparse.ArgumentParser(description="Upload video to YouTube")
    parser.add_argument("--file", required=True, help="Path to video file (.mp4)")
    parser.add_argument("--title", required=True, help="Video title")
    parser.add_argument("--description", default="", help="Video description")
    parser.add_argument("--tags", default="pdf,manga,reader,android,shorts", help="Comma-separated tags")
    args = parser.parse_args()

    if not os.path.exists(args.file):
        print(f"Error: Video file not found at {args.file}")
        sys.exit(1)

    print("To upload via YouTube Data API v3:")
    print("1. Create a project in Google Cloud Console & enable YouTube Data API v3.")
    print("2. Create OAuth 2.0 Client ID credentials and save as client_secrets.json.")
    print("3. Install dependencies: pip install google-auth google-auth-oauthlib google-api-python-client")
    print("4. Use resumable upload flow with googleapiclient.http.MediaFileUpload.")
    print(f"\nTarget file ready for upload: {os.path.abspath(args.file)}")

if __name__ == "__main__":
    main()
