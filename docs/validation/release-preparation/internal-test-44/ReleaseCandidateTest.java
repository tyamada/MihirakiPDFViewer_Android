package com.github.tyamada.viewerreleasevalidation;
import android.content.*;
import android.net.Uri;
import android.graphics.Rect;
import android.os.SystemClock;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import com.google.android.apps.common.testing.accessibility.framework.*;
import com.google.android.apps.common.testing.accessibility.framework.uielement.*;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.*;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.pdmodel.encryption.*;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.*;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.json.*;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ReleaseCandidateTest {
 static final String APP="com.github.tyamada.MihirakiPDFViewer_Android";
 final android.app.Instrumentation inst=InstrumentationRegistry.getInstrumentation();
 final Context ctx=inst.getTargetContext();
 final UiDevice device=UiDevice.getInstance(inst);
 final File out=ctx.getExternalFilesDir(null);
 UiObject2 find(BySelector by) { UiObject2 o=device.wait(Until.findObject(by),15000);assertNotNull("Missing "+by,o);return o; }
 UiObject2 sampleButton(){
  UiObject2 o=device.findObject(By.desc("Sample PDFs"));
  return o!=null ? o : find(By.text("Sample PDFs"));
 }
 void showChrome(){
  SystemClock.sleep(1500);
  if(device.hasObject(By.clazz("android.widget.EditText")))return;
  for(int attempt=0;attempt<3;attempt++){
   device.click(device.getDisplayWidth()/2,device.getDisplayHeight()/2);
   if(device.wait(Until.hasObject(By.clazz("android.widget.EditText")),3000))return;
  }
 }
 void clickText(String text){find(By.text(text)).click();device.waitForIdle();}
 void dismissTutorial() {
  UiObject2 skip=device.wait(Until.findObject(By.text("Skip")),10000);
  if(skip!=null){skip.click();assertTrue(device.wait(Until.gone(By.text("Skip")),15000));device.waitForIdle();}
 }
 void launch() throws Exception {
  device.executeShellCommand("am force-stop "+APP);
  Intent i=ctx.getPackageManager().getLaunchIntentForPackage(APP);assertNotNull(i);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);ctx.startActivity(i);
  device.wait(Until.hasObject(By.pkg(APP)),15000);dismissTutorial();
  if(!device.hasObject(By.text("Sample PDFs")) && !device.hasObject(By.desc("Sample PDFs")))showChrome();
 }
 void capture(String name) throws Exception {
  device.waitForIdle();SystemClock.sleep(1000);device.takeScreenshot(new File(out,name+".png"));device.dumpWindowHierarchy(new File(out,name+".xml"));
  var root=inst.getUiAutomation().getRootInActiveWindow();
  var hierarchy=AccessibilityHierarchyAndroid.newBuilder(root,ctx).setObtainCharacterLocations(true).build();JSONArray results=new JSONArray();
  Parameters params=new Parameters();
  params.putScreenCapture(new com.google.android.apps.common.testing.accessibility.framework.utils.contrast.BitmapImage(android.graphics.BitmapFactory.decodeFile(new File(out,name+".png").getAbsolutePath())));
  params.putEnableEnhancedContrastEvaluation(true);
  for(var check:AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(AccessibilityCheckPreset.LATEST)){
   for(var result:check.runCheckOnHierarchy(hierarchy,null,params)){
    JSONObject j=new JSONObject();j.put("check",check.getClass().getSimpleName());j.put("type",result.getType().name());j.put("message",result.getMessage(Locale.ENGLISH).toString());
    if(result.getElement()!=null){j.put("text",String.valueOf(result.getElement().getText()));j.put("description",String.valueOf(result.getElement().getContentDescription()));j.put("bounds",String.valueOf(result.getElement().getBoundsInScreen()));}
    results.put(j);
   }
  }
  try(var fw=new FileWriter(new File(out,name+"-a11y.json"))){fw.write(results.toString(2));}
  device.executeShellCommand("mkdir -p /sdcard/Download/viewer-release-validation-complete");
  device.executeShellCommand("cp -r "+out.getAbsolutePath()+" /sdcard/Download/viewer-release-validation-complete/");
 }
 void assertFirstEpisode(int episode){
  var titles=device.findObjects(By.textContains("EPISODE"));assertFalse(titles.isEmpty());
  titles.sort(Comparator.comparingInt(x->x.getVisibleBounds().top));
  assertTrue(titles.get(0).getText(),titles.get(0).getText().contains("EPISODE "+episode));
 }
 @Test public void a_launchSortDownloadAndPersist() throws Exception {
  try {
   launch();capture("01-launch");sampleButton().click();
   find(By.textContains("From Latest Chapter"));assertFirstEpisode(3);capture("02-samples-latest");
   clickText("Order: From Latest Chapter");find(By.textContains("From Chapter 1"));assertFirstEpisode(1);capture("03-samples-first");
   device.executeShellCommand("am force-stop "+APP);launch();sampleButton().click();find(By.textContains("From Chapter 1"));
   clickText("Order: From Chapter 1");
   var previous=device.findObject(By.descStartsWith("Delete: THE TRY-IT CLUB EPISODE 3"));
   if(previous!=null){previous.click();find(By.descStartsWith("Download: THE TRY-IT CLUB EPISODE 3"));}
   var downloads=device.findObjects(By.text("Download"));
   if(!downloads.isEmpty()){
    downloads.sort(Comparator.comparingInt(x->x.getVisibleBounds().top));downloads.get(0).click();
    device.wait(Until.hasObject(By.textContains("Downloading")),5000);
    assertTrue("Download must finish",device.wait(Until.gone(By.textContains("Downloading")),120000));
   }
   capture("04-sample-downloaded");find(By.descStartsWith("Open: THE TRY-IT CLUB EPISODE 3")).click();
   device.wait(Until.gone(By.text("Language Filter")),15000);showChrome();
   find(By.clazz("android.widget.EditText"));find(By.desc(java.util.regex.Pattern.compile("Page [0-9]+ of [0-9]+")));capture("05-sample-opened");
  } finally {capture("a-final");}
 }
 void fixture(String name, boolean encrypted) throws Exception {
  PDFBoxResourceLoader.init(ctx);
  try(PDDocument doc=new PDDocument()){
   var outline=new PDDocumentOutline();doc.getDocumentCatalog().setDocumentOutline(outline);
   for(int n=0;n<2;n++){
    PDPage page=new PDPage();doc.addPage(page);
    try(PDPageContentStream stream=new PDPageContentStream(doc,page)){
     stream.beginText();stream.setFont(PDType1Font.HELVETICA,22);stream.newLineAtOffset(50,700);stream.showText("Release verification page "+(n+1));stream.endText();
    }
    var entry=new PDOutlineItem();entry.setTitle("Chapter "+(n+1));entry.setDestination(page);outline.addLast(entry);
   }
   if(encrypted){var policy=new StandardProtectionPolicy("test-owner","test-reader",new AccessPermission());policy.setEncryptionKeyLength(128);doc.protect(policy);}
   doc.save(new File(ctx.getCacheDir(),name));
  }
 }
 void openFixture(String name){
  Uri uri=Uri.parse("content://com.github.tyamada.viewerreleasevalidation.fixtures/"+name);
  Intent i=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/pdf").setPackage(APP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_GRANT_READ_URI_PERMISSION);
  ctx.startActivity(i);dismissTutorial();device.waitForIdle();
 }
 @Test public void b_pdfPasswordSearchAndOutline() throws Exception {
  try{
   fixture("plain.pdf",false);fixture("encrypted.pdf",true);launch();openFixture("plain.pdf");showChrome();
   UiObject2 input=find(By.clazz("android.widget.EditText"));input.setText("verification");find(By.desc("Search result 1 of 2"));capture("06-search");
   find(By.desc("Table of contents")).click();clickText("Chapter 2");find(By.desc("Page 2 of 2"));capture("07-outline-page2");
   openFixture("encrypted.pdf");find(By.text("Password required"));find(By.clazz("android.widget.EditText")).setText("wrong");clickText("Open");find(By.text("Incorrect password."));
   find(By.clazz("android.widget.EditText")).setText("test-reader");clickText("Open");showChrome();find(By.desc("Table of contents"));capture("08-encrypted-rendered");
  }finally{capture("b-final");}
 }
 @Test public void c_largeFontAndKeyboard() throws Exception {
  String original=device.executeShellCommand("settings get system font_scale").trim();
  try{
   device.executeShellCommand("settings put system font_scale 2.0");launch();
   sampleButton().click();find(By.textContains("Order:"));capture("09-large-font-samples");
   JSONArray keyboard=new JSONArray();Set<String> positions=new HashSet<>();
   for(int n=0;n<8;n++){
    device.pressKeyCode(android.view.KeyEvent.KEYCODE_TAB);device.waitForIdle();
    var root=inst.getUiAutomation().getRootInActiveWindow();
    if(root!=null){var focused=root.findFocus(android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT);
     if(focused!=null){Rect bounds=new Rect();focused.getBoundsInScreen(bounds);String value=bounds+" "+focused.getText()+" "+focused.getContentDescription();positions.add(value);keyboard.put(value);}
    }
   }
   try(var fw=new FileWriter(new File(out,"keyboard-focus.json"))){fw.write(keyboard.toString(2));}
   capture("10-keyboard-focus");assertTrue("Keyboard focus must move between controls",positions.size()>1);
  }finally{device.executeShellCommand("settings put system font_scale "+original);}
 }
 @Test public void e_narrowPhoneLargeFont() throws Exception {
  String original=device.executeShellCommand("settings get system font_scale").trim();
  try{
   device.executeShellCommand("wm size 720x1280");device.executeShellCommand("settings put system font_scale 2.0");SystemClock.sleep(1000);
   launch();sampleButton().click();find(By.textContains("Order:"));capture("12-narrow-samples");
   fixture("plain.pdf",false);openFixture("plain.pdf");showChrome();
   UiObject2 input=find(By.clazz("android.widget.EditText"));
   assertTrue("Search input must remain visible",input.getVisibleBounds().width()>0);
   input.setText("verification");capture("13-narrow-reader");find(By.desc("Search result 1 of 2"));
  }finally{device.executeShellCommand("settings put system font_scale "+original);device.executeShellCommand("wm size reset");}
 }

 @Test public void f_supportLargeFontScrollReachability() throws Exception {
  String originalFont=device.executeShellCommand("settings get system font_scale").trim();
  String originalSize=device.executeShellCommand("wm size").trim();
  try{
   device.executeShellCommand("wm size 640x960");
   device.executeShellCommand("settings put system font_scale 2.0");
   launch();find(By.desc("Settings")).click();clickText("Support");
   capture("14a-support-large-font-initial");
   UiObject2 scroll=find(By.scrollable(true));
   // UiObject2.scroll can report false after a successful Compose scroll.
   // Drive real swipes and judge reachability from the final visible controls.
   for(int n=0;n<10;n++){
    Rect area=find(By.scrollable(true)).getVisibleBounds();
    assertTrue(device.swipe(area.centerX(),area.bottom-40,area.centerX(),area.top+40,24));
    device.waitForIdle();
   }
   capture("14-support-large-font-bottom");
   UiObject2 last=find(By.textContains("Koyomi"));
   Rect bounds=last.getVisibleBounds();assertTrue(bounds.width()>0 && bounds.height()>0);
   var buttons=device.findObjects(By.text("Purchase"));assertFalse("Purchase control must remain reachable",buttons.isEmpty());
   for(int n=0;n<10;n++){
    Rect area=find(By.scrollable(true)).getVisibleBounds();
    assertTrue(device.swipe(area.centerX(),area.top+40,area.centerX(),area.bottom-40,24));
    device.waitForIdle();
   }
   find(By.text("All features are available without purchase."));
   capture("15-support-large-font-top");
   find(By.desc("Back")).click();find(By.desc("Settings"));
  }finally{
   device.executeShellCommand("settings put system font_scale "+originalFont);
   java.util.regex.Matcher match=java.util.regex.Pattern.compile("Override size: (\\d+x\\d+)").matcher(originalSize);
   device.executeShellCommand(match.find()?"wm size "+match.group(1):"wm size reset");
  }
 }

}
