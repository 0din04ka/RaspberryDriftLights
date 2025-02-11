module ru.emelyantsev.raspberrydriftlights {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.pi4j;
    requires org.slf4j;


    opens ru.emelyantsev.raspberrydriftlights to javafx.fxml;
    exports ru.emelyantsev.raspberrydriftlights;
}