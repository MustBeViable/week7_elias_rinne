package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.ResultService;

public class CalculatorController {

    @FXML private TextField number1Field;
    @FXML private TextField number2Field;
    @FXML private Label resultLabel;

    @FXML
    private void onCalculateClick() {
        try {
            double num1 = Double.parseDouble(number1Field.getText());
            double num2 = Double.parseDouble(number2Field.getText());

            double sum = num1 + num2;
            double product = num1 * num2;
            double subtraction = num1 - num2;

            // Division: null jos jakaja on 0 (näytetään "undefined", tallennetaan NULL DB:hen)
            Double division = (num2 == 0.0) ? null : (num1 / num2);

            String divisionText = (division == null) ? "undefined (division by 0)" : String.valueOf(division);

            resultLabel.setText(
                    "Sum: " + sum +
                            "\nProduct: " + product +
                            "\nSubtraction: " + subtraction +
                            "\nDivision: " + divisionText
            );

            // Save to DB (myös subtraction + division)
            ResultService.saveResult(num1, num2, sum, product, subtraction, division);

        } catch (NumberFormatException e) {
            resultLabel.setText("Please enter valid numbers!");
        }
    }
}