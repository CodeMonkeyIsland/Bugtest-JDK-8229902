import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.concurrent.Worker.State;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;

class Bug {
    public static void main (String args[]) {
        Platform.startup(() -> {});
        Platform.runLater(() -> {
            WebView webView = new WebView();
            Stage stage = new Stage();
            stage.setScene(new Scene(webView));
            stage.setAlwaysOnTop(true);
            stage.setWidth(620);
            stage.setHeight(640);
            WebEngine engine = webView.getEngine();
            String content="<html><body><canvas id=\"canvas\" width=\"600\" height=\"600\" style=\"border: 1px solid red;\"></canvas><script>window.onload = function() {";
            content=content
                    + " var canvas = document.getElementById('canvas');"
                	+ " var c = canvas.getContext('2d');"
               		+ " var size = 4;"
                	+ " var step = size + 2;"
                	+ " for (var y = 0; y <= canvas.height; y = y + step) {"
                		+ " for (var x = 0; x <= canvas.width; x = x + step) {"
                			+ " c.fillRect(x, y, size, size);"
                		+ " }"
                    + " }";
            content=content+"}</script></body></html>";
            engine.loadContent(content);
            stage.show();
        });
    }
}
