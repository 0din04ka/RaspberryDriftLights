module ru.emelyantsev.raspberrydriftlights {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.emelyantsev.raspberrydriftlights to javafx.fxml;
    exports ru.emelyantsev.raspberrydriftlights;
}