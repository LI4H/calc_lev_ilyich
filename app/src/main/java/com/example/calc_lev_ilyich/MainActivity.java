package com.example.calc_lev_ilyich;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView previewTextView;
    private TextView mainTextView;
    private boolean isEqualPressed = false;
    private boolean lastInputIsOperator = false;
    private boolean lastInputIsPercent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewTextView = findViewById(R.id.textView);
        mainTextView = findViewById(R.id.textView2);

        // кнопка очистки
        findViewById(R.id.clear_button).setOnClickListener(v -> clear());

        // Кнопка плюс/минус
        findViewById(R.id.plus_minus_button).setOnClickListener(v -> toggleSign());

        // Кнопка процента
        findViewById(R.id.percent_button).setOnClickListener(v -> calculatePercent());

        // Операторы
        findViewById(R.id.divide_button).setOnClickListener(v -> selectOperation('÷'));
        findViewById(R.id.multiply_button).setOnClickListener(v -> selectOperation('x'));
        findViewById(R.id.minus_button).setOnClickListener(v -> selectOperation('-'));
        findViewById(R.id.plus_button).setOnClickListener(v -> selectOperation('+'));

        // Кнопка равно
        findViewById(R.id.equal_button).setOnClickListener(v -> calculateResult());

        // Цифры и точка
        findViewById(R.id.button_0).setOnClickListener(v -> appendNumber("0"));
        findViewById(R.id.button_1).setOnClickListener(v -> appendNumber("1"));
        findViewById(R.id.button_2).setOnClickListener(v -> appendNumber("2"));
        findViewById(R.id.button_3).setOnClickListener(v -> appendNumber("3"));
        findViewById(R.id.button_4).setOnClickListener(v -> appendNumber("4"));
        findViewById(R.id.button_5).setOnClickListener(v -> appendNumber("5"));
        findViewById(R.id.button_6).setOnClickListener(v -> appendNumber("6"));
        findViewById(R.id.button_7).setOnClickListener(v -> appendNumber("7"));
        findViewById(R.id.button_8).setOnClickListener(v -> appendNumber("8"));
        findViewById(R.id.button_9).setOnClickListener(v -> appendNumber("9"));
        findViewById(R.id.dot_button).setOnClickListener(v -> appendDot());
    }

    // Функция доавления цифры
    private void appendNumber(String number) {
        if (isEqualPressed) {
            previewTextView.setText("0");
            mainTextView.setText("");
            isEqualPressed = false;
        }
        String currentText = mainTextView.getText().toString();
        if (currentText.equals("0") && !number.equals(".")) {
            mainTextView.setText(number);
        } else {
            mainTextView.setText(currentText + number);
        }
        lastInputIsOperator = false;
        lastInputIsPercent = false;
        updatePreview();
    }

    // Функция добавления точки
    private void appendDot() {
        if (isEqualPressed) {
            previewTextView.setText("0");
            mainTextView.setText("");
            isEqualPressed = false;
        }
        String currentText = mainTextView.getText().toString();
        if (!currentText.endsWith(".") && !lastInputIsOperator && !currentText.endsWith("%")) {
            String[] parts = currentText.split(" ");
            String lastPart = parts[parts.length - 1];
            if (!lastPart.contains(".")) {
                mainTextView.setText(currentText + ".");
            }
        }
        updatePreview();
    }

    // Функция очистки
    private void clear() {
        mainTextView.setText("0");
        previewTextView.setText("0");
        isEqualPressed = false;
        lastInputIsOperator = false;
        lastInputIsPercent = false;
    }

    // Функция переключения знака
    private void toggleSign() {
        String currentText = mainTextView.getText().toString();
        if (!currentText.isEmpty() && !currentText.equals("0")) {
            if (currentText.startsWith("-")) {
                mainTextView.setText(currentText.substring(1));
            } else {
                mainTextView.setText("-" + currentText);
            }
        }
        updatePreview();
    }

    // Функция вычисления процента
    private void calculatePercent() {
        if (lastInputIsPercent) {
            return; //предотвращение множества последовательных операций процента
        }
        String currentText = mainTextView.getText().toString();
        String[] parts = currentText.split(" ");
        String lastPart = parts[parts.length - 1];
        try {
            double value = Double.parseDouble(lastPart);
            value = value / 100;
            mainTextView.setText(currentText + "%");
            previewTextView.setText(String.valueOf(value));
            isEqualPressed = false;
            lastInputIsOperator = false;
            lastInputIsPercent = true;
        } catch (NumberFormatException e) {
            previewTextView.setText("Error");
        }
    }

    // Функция выбора операции
    private void selectOperation(char operation) {
        if (!isEqualPressed && !lastInputIsOperator && !lastInputIsPercent) {
            String currentText = mainTextView.getText().toString();
            mainTextView.setText(currentText + " " + operation + " ");
            lastInputIsOperator = true;
        } else if (isEqualPressed) {
            mainTextView.setText(previewTextView.getText().toString() + " " + operation + " ");
            isEqualPressed = false;
            lastInputIsOperator = true;
        }
        updatePreview();
    }

    // Функция вычисления результта
    private void calculateResult() {
        String currentText = mainTextView.getText().toString();
        try {
            double result = eval(currentText);
            previewTextView.setText(String.valueOf(result));
            mainTextView.setText(previewTextView.getText().toString());
            isEqualPressed = true;
            lastInputIsOperator = false;
            lastInputIsPercent = false;
        } catch (Exception e) {
            previewTextView.setText("Error");
            mainTextView.setText("");
        }
    }
    // Функция оценки выражения
    private double eval(String expression) {
        return new Object() {
            int pos = -1, ch;

            // Переход к следующему символу
            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            // Пропуск пробелов и проверка символа
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            // Парсинг выражения
            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Парсинг выражения с учетом операторов + и -
            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // сложение
                    else if (eat('-')) x -= parseTerm(); // вычитание
                    else return x;
                }
            }

            // Парсинг терма с учетом операторов * и /
            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('x')) x *= parseFactor(); // умножение
                    else if (eat('÷')) x /= parseFactor(); // деление
                    else return x;
                }
            }

            // Парсинг факторов, включая унарные + и -, скобки, числа и проценты
            double parseFactor() {
                if (eat('+')) return parseFactor(); // унарный плюс
                if (eat('-')) return -parseFactor(); // унарный минус

                double x;
                int startPos = this.pos;
                if (eat('(')) { // скобки
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // числа
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else if (ch == '%') { // процент
                    nextChar();
                    x = parseFactor() / 100;
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                return x;
            }
        }.parse();
    }

    // Обновление предварительного просмотра результата
    private void updatePreview() {
        String currentText = mainTextView.getText().toString();
        try {
            double result = eval(currentText);
            previewTextView.setText(String.valueOf(result));
        } catch (Exception e) {
            previewTextView.setText("Error");
        }
    }
}
