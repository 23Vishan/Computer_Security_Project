import java.awt.Color;
import java.awt.Font;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.awt.Dimension;
import javax.swing.*;

public class SurveyPanel extends JPanel {
	
	ArrayList<JTextField> textFields;
	ArrayList<ButtonGroup> radioHealthFields;
	ArrayList<ButtonGroup> radioFrequencyFields;
	ArrayList<JCheckBox> checkboxFields;
	
	ArrayList<String> textFieldQuestions;
	ArrayList<String> radioHealthQuestions;
	ArrayList<String> radioFrequencyQuestions;
	ArrayList<String> checkboxQuestions;

	public SurveyPanel() {
		this.setBackground(new Color(255, 255, 255));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.textFields = new ArrayList<JTextField>();
		this.radioHealthFields = new ArrayList<ButtonGroup>();
		this.radioFrequencyFields = new ArrayList<ButtonGroup>();
		this.checkboxFields = new ArrayList<JCheckBox>();
		this.textFieldQuestions = new ArrayList<String>();
		this.radioHealthQuestions = new ArrayList<String>();
		this.radioFrequencyQuestions = new ArrayList<String>();
		this.checkboxQuestions = new ArrayList<String>();
	}
	
	public JPanel getPanel() {
		return this;
	}
	
	public void addTextField(int columns, String text) {
		addLabel("\0"+text);
		textFieldQuestions.add(text);
		
		JTextField textfield = new JTextField();
		textfield.setColumns(columns);
		textfield.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		textfield.setFont(new Font("Arial", Font.PLAIN, 11));
		textfield.setBackground(new Color(245, 249, 255));
		textfield.setPreferredSize(new Dimension(200, 25));
		textFields.add(textfield);
		this.add(textfield);
	}
	
	public void addLabel(String text) {
		boolean isRadioTitle = false;
		if (text.charAt(0) == '\0') {
			text = text.substring(1);
			isRadioTitle = true;
		}
		
		JLabel label = new JLabel(text);
		if (!isRadioTitle) {
			label.setFont(new Font("Arial", Font.BOLD, 12));
		} else {
			label.setFont(new Font("Arial", Font.ITALIC, 12));
		}
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(Box.createVerticalStrut(10));
		this.add(label);
	}
	
	public void addTitle(String text) {
		JLabel title = new JLabel(text);
		title.setFont(new Font("Arial", Font.BOLD, 16));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(Box.createVerticalStrut(5));
		this.add(title);
		this.add(Box.createVerticalStrut(5));
	}
	
	public void addRadioHealth(String text) {
		addLabel("\0"+text);
		radioHealthQuestions.add(text);
		
		JRadioButton r1,r2,r3;
		r1 = new JRadioButton();
		r2 = new JRadioButton();
		r3 = new JRadioButton();
		
		r1.setText("Not Healthy");
		r2.setText("Somewhat Healthy");
		r3.setText("Very Healthy");

		Font font = new Font("Arial", Font.PLAIN, 12);
		r1.setFont(font);
		r2.setFont(font);
		r3.setFont(font);

		r1.setBackground(Color.WHITE);
		r2.setBackground(Color.WHITE);
		r3.setBackground(Color.WHITE);

		ButtonGroup bg = new ButtonGroup();
		bg.add(r1);
		bg.add(r2);
		bg.add(r3);
		
		this.add(r1);
		this.add(r2);
		this.add(r3);
		radioHealthFields.add(bg);
	}
	
	public void addRadioFrequency(String text) {
		addLabel("\0"+text);
		radioFrequencyQuestions.add(text);
		JRadioButton r1,r2,r3,r4,r5;
		
		r1 = new JRadioButton();
		r2 = new JRadioButton();
		r3 = new JRadioButton();
		r4 = new JRadioButton();
		r5 = new JRadioButton();
		
		r1.setText("Never");
		r2.setText("Occasionally");
		r3.setText("Sometimes");
		r4.setText("Frequently");
		r5.setText("All the Time");

		ButtonGroup bg = new ButtonGroup();
		bg.add(r1);
		bg.add(r2);
		bg.add(r3);
		bg.add(r4);
		bg.add(r5);

		Font font = new Font("Arial", Font.PLAIN, 12);
		r1.setFont(font);
		r2.setFont(font);
		r3.setFont(font);
		r4.setFont(font);
		r5.setFont(font);

		r1.setBackground(Color.WHITE);
		r2.setBackground(Color.WHITE);
		r3.setBackground(Color.WHITE);
		r4.setBackground(Color.WHITE);
		r5.setBackground(Color.WHITE);
		
		this.add(r1);
		this.add(r2);
		this.add(r3);
		this.add(r4);
		this.add(r5);
		radioFrequencyFields.add(bg);
	}
	
	public void addCheckbox(String text) {
		JCheckBox checkbox = new JCheckBox(text);

		Font font = new Font("Arial", Font.PLAIN, 12);
		checkbox.setBackground(Color.WHITE);
		checkbox.setFont(font);

		checkboxQuestions.add(text);
		this.add(checkbox);
	}
	
	public JButton addNextButton() {
        JButton nextButton = new JButton("Next");
        nextButton.setFont(new Font("Arial", Font.BOLD, 12));
        Color green = new Color(51, 166, 82);
        nextButton.setBackground(green);
        nextButton.setForeground(Color.WHITE);
		this.add(nextButton);
		return nextButton;
	}
	
	public JButton addSubmitButton() {
        JButton submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.BOLD, 12));
		Color green = new Color(51, 166, 82);
        submitButton.setBackground(green);
        submitButton.setForeground(Color.WHITE);

        this.add(submitButton);
        return submitButton;
	}
	
    public String getSurveyData() {
    	StringBuilder surveyData = new StringBuilder();
    	
		// add text field data
    	for (int i = 0; i < textFieldQuestions.size(); i++)  {
    		surveyData.append(textFieldQuestions.get(i));
			surveyData.append(" ");
			surveyData.append(textFields.get(i).getText());
			surveyData.append("\n");
    	}
    	
		// add radio button health data
    	for (int i = 0; i < radioHealthQuestions.size(); i++)  {
    		surveyData.append(radioHealthQuestions.get(i));
			surveyData.append(" ");
    		for (Enumeration<AbstractButton> buttons = radioHealthFields.get(i).getElements(); buttons.hasMoreElements();) {
                AbstractButton button = buttons.nextElement();

                if (button.isSelected()) {
                    button.getText();
        			surveyData.append(button.getText());
        			surveyData.append("\n");
                }
            }
    	}
		
		// add radio button frequency data
    	for (int i = 0; i < radioFrequencyQuestions.size(); i++)  {
    		surveyData.append(radioFrequencyQuestions.get(i));
			surveyData.append(" ");
    		for (Enumeration<AbstractButton> buttons = radioFrequencyFields.get(i).getElements(); buttons.hasMoreElements();) {
                AbstractButton button = buttons.nextElement();

                if (button.isSelected()) {
                    button.getText();
        			surveyData.append(button.getText());
        			surveyData.append("\n");
                }
            }
    	}
    	
		// add checkbox data
		int size = Math.min(checkboxQuestions.size(), checkboxFields.size());
		for (int i = 0; i < size; i++)  {
			surveyData.append(checkboxQuestions.get(i));
			surveyData.append(" ");
			surveyData.append(checkboxFields.get(i).getText());
			surveyData.append("\n");
		}

       	return surveyData.toString();
    }
}
