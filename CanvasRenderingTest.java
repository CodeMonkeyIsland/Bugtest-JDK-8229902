import org.junit.Test;
import static org.junit.Assert.*;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.concurrent.Worker.State;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;

import javafx.concurrent.Worker;
import javafx.scene.Group;
import netscape.javascript.JSObject;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import java.lang.reflect.Type;

import java.util.Arrays;

public class CanvasRenderingTest {
	
   int canvasWidth=600;
   int canvasHeight=600;
   int[] pixelArrayShouldBe=null;
   int[] pixelArrayTest=null;

   int size=4;
   int step=size+2;

   CanvasRenderingTest t;

   @Test
   public void testCanvasRendering() {
      t=this;
      setupPixelArrayShouldBe();
      setupWindow();
      long time=System.currentTimeMillis();
      while (pixelArrayTest==null){
         if (System.currentTimeMillis()-time > 1000){
            break;
         }
      }
      //not closing the window for now for visual inspection
      //Platform.exit();
      assertArrayEquals(pixelArrayShouldBe, pixelArrayTest);

   }

   //this is how the Array is supposed to look like
   private void setupPixelArrayShouldBe(){
      pixelArrayShouldBe=new int[canvasWidth*canvasHeight*4];
      Arrays.fill(pixelArrayShouldBe, 0); //white background
      for (int y = 0; y <= canvasHeight; y = y + step) {
         for (var x = 0; x <= canvasWidth; x = x + step) {

            //"painting" the shapes
            for (int xPaint=x; xPaint<x+size;xPaint++){
               for (int yPaint=y; yPaint<y+size;yPaint++){
               
                  if (yPaint < canvasHeight && xPaint < canvasWidth){
                     int position=((yPaint*canvasWidth)+xPaint)*4;
                     pixelArrayShouldBe[position]=0; //r
                     pixelArrayShouldBe[position+1]=0; //g
                     pixelArrayShouldBe[position+2]=0; //b
                     pixelArrayShouldBe[position+3]=255; //alpha
                  }
               }
            }

         }
      }
   }

   //this is the bug-test from https://bugs.openjdk.org/browse/JDK-8229902
   //added a hook, so it sets pixelArrayTest in this Class after its finished painting
   //engine.loadContent is async!
   private void setupWindow(){
      Platform.startup(() -> {});
        Platform.runLater(() -> {
            WebView webView = new WebView();
            Stage stage = new Stage();
            stage.setScene(new Scene(webView));
            stage.setAlwaysOnTop(true);
            stage.setWidth(620);
            stage.setHeight(640);
            WebEngine engine = webView.getEngine();
            engine.setJavaScriptEnabled( true );
            engine.setOnAlert(event -> {
               Alert alert = new Alert(Alert.AlertType.INFORMATION);
               alert.setContentText(event.getData());
               alert.showAndWait();
            });
            String content="<html><body><canvas id=\"canvas\" width=\""+canvasWidth+"\" height=\""+canvasHeight+"\" style=\"border: 1px solid red;\"></canvas><script>window.onload = function() {";
            content=content+"}</script></body></html>";
            engine.loadContent(content);
            engine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
               public void changed(ObservableValue<? extends State> observableValue, State oldState, State newState) {
                  if (newState == State.SUCCEEDED) {
                     JSObject window = (JSObject) engine.executeScript("window");
                     window.setMember("unitTest", t);
                     engine.executeScript(""
                        + " var canvas = document.getElementById('canvas');"
                        + " var c = canvas.getContext('2d');"
                        + " var size = "+size+";"
                        + " var step = "+step+";"
                        + " for (var y = 0; y <= canvas.height; y = y + step) {"
                           + " for (var x = 0; x <= canvas.width; x = x + step) {"
                              + " c.fillRect(x, y, size, size);"
                           + " }"
                        + " }"
                        + " var stringifiedImageData= c.getImageData(0,0,"+canvasWidth+","+canvasHeight+").data.toString();"
                        + " unitTest.setTestArray(stringifiedImageData);");

                  }
               }
            });
            stage.show();
        });
   }

   public void setTestArray(String stringifiedImageData){
      String[] stringArray = stringifiedImageData.split(",");
      pixelArrayTest=new int[stringArray.length];
      for (int i=0;i< stringArray.length;i++){
         pixelArrayTest[i]=Integer.parseInt(stringArray[i]);
      }
   }


}