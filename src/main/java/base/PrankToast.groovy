package base

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration

/**
 * I don't actually know what this class does.
 */
@CompileStatic
final class PrankToast {

    static void addText(Stage ownerStage, String toastMsg, int toastDelay, int fadeInDelay, int fadeOutDelay) {
        Stage toastStage = new Stage()

        toastStage.initOwner(ownerStage)
        toastStage.resizable = false
        toastStage.initStyle(StageStyle.TRANSPARENT)

        Text text = new Text(toastMsg)
        text.font = Font.font('Verdana', 40)
        text.fill = Color.RED

        StackPane root = new StackPane(text)
        root.style = '-fx-background-radius: 20; -fx-background-color: rgba(0, 0, 0, 0.2); -fx-padding: 50px;'
        root.opacity = 0

        Scene scene = new Scene(root)
        scene.fill = Color.TRANSPARENT
        toastStage.scene = scene
        toastStage.show()

        Timeline fadeInTimeline = new Timeline()
        KeyFrame fadeInKey1 = new KeyFrame(
            Duration.millis(fadeInDelay),
            new KeyValue(toastStage.scene.root.opacity, 1)
        )
        fadeInTimeline.keyFrames.add(fadeInKey1)
        fadeInTimeline.onFinished = ((ae) -> {
            new Thread(() -> {
                try {
                    Thread.sleep(toastDelay)
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }

                Timeline fadeOutTimeline = new Timeline()
                KeyFrame fadeOutKey1 = new KeyFrame(
                    Duration.millis(fadeOutDelay),
                    new KeyValue(toastStage.scene.root.opacity, 0)
                )
                fadeOutTimeline.keyFrames.add(fadeOutKey1)
                fadeOutTimeline.onFinished = (aeb) -> toastStage.close()
                fadeOutTimeline.play()
            }).start()
        })
        fadeInTimeline.play()
    }

}
