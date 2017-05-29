package toolWindow;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

import java.util.regex.Pattern;

/**
 * Created by Jed on 29-May-17.
 */
public abstract class NumericField extends TextField {

    public abstract void updateAction(String stringValue);


    private static Pattern pattern = Pattern.compile("");

    public NumericField() {
        setListener();
    }

    public NumericField(String text) {
        super(text);
        setListener();
    }


    private void setListener(){
        this.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!newValue.toString().matches("-?((\\d*)|(\\d+\\.\\d*))")){
                    newValue = oldValue;
                }else if(!newValue.equals("")){
                    if(Double.parseDouble(newValue) > 1.0){
                        newValue = "1.0";
                    }else if(Double.parseDouble(newValue) < -1.0){
                        newValue = "-1.0";
                    }
                }

                setText(newValue);
                if(!newValue.equals("")){
                    updateAction(newValue);
                }
            }
        });
    }
}
