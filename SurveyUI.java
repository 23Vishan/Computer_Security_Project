import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SurveyUI {

	static String filePath = "C:\\Users\\visha\\Desktop\\Project\\survey_questions.csv";

	JButton submitButton;    
	ArrayList<JPanel> panels;
	JFrame frame;
	JScrollPane scrollPane;
	int panelCounter;

	JPanel bottomPanel;
	JButton nextButton;
	JButton prevButton;
	
	public SurveyUI() {
		panels = new ArrayList<JPanel>();
		frame = new JFrame("Healthcare");
		panelCounter = 0;
		scrollPane = new JScrollPane();
	}

    public String getSurveyData() {
    	String data = "";
		for (JPanel panel : panels) {
			SurveyPanel surveyPanel = (SurveyPanel) panel;
			data += surveyPanel.getSurveyData();
		}
		//System.out.println(data);
		return data;
    }
    
    public JButton getSubmitButton() {
    	return submitButton;
    }
    
    public static ArrayList<String> getConfig(String path) {
    	 // Specify the path to the CSV file
    	ArrayList<String> fields = new ArrayList<String>();
        try {

            // Create a new File object
            File file = new File(path);
            Scanner scanner = new Scanner(file);

            // Loop through each line in the file
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                
                String[] data = line.split(",");
                for (String s : data) {
                    fields.add(s); 
            	}
            }

            // Close the scanner
            scanner.close();
        } 
        catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
            e.printStackTrace();
        }
        return fields;
    }
    
    private void createPanels(ArrayList<String> config) {
    	if (config == null ) {
    		return;
    	}
    	
    	SurveyPanel surveyPanel = new SurveyPanel();
    	Iterator iter = config.iterator();
    	boolean firstPage = true;
    	
    	while(iter.hasNext()) {
    		String type = (String) iter.next();
    		String text;
    		switch(type) {
    			case "PAGE":
    				if (!firstPage) {
        				panels.add(surveyPanel);
    				}
    				firstPage = false;
    				surveyPanel = new SurveyPanel();
    				break;
    			case "title":
    				text = (String) iter.next();
    				surveyPanel.addTitle(text);
    				break;
    			case "label":
    				text = (String) iter.next();
    				surveyPanel.addLabel(text);
    				break;
    			case "text":
    				text = (String) iter.next();
    				surveyPanel.addTextField(30, text);
    				break;
    			case "radioHealth":
    				text = (String) iter.next();
    				surveyPanel.addRadioHealth(text);
    				break;
    			case "radioFrequency":
    				text = (String) iter.next();
    				surveyPanel.addRadioFrequency(text);
    				break;
				case "checkboxLabel":
					text = (String) iter.next();
					surveyPanel.addLabel("\0"+text);
					break;
    			case "checkbox":
    				text = (String) iter.next();
    				surveyPanel.addCheckbox(text);
    				break;
    			case "next":
    				break;
    			case "submit":
    				break;
    			default:
    				break;
    		}
    	}

		// add the final panel
		if (surveyPanel != null) {
			panels.add(surveyPanel);
		}

		//System.out.println(panels.size());
    }

	private void createButtomPanel() {
		bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		bottomPanel.setBackground(Color.WHITE);

		// Create submit button
		submitButton = new JButton("Submit");
		submitButton.setFont(new Font("Arial", Font.BOLD, 12));
		Color green = new Color(51, 166, 82);
		submitButton.setBackground(green);
		submitButton.setForeground(Color.WHITE);
		submitButton.setPreferredSize(new Dimension(80, 25));
		bottomPanel.add(submitButton);
		submitButton.setVisible(false);

		// Create next button
		nextButton = new JButton("Next");
        nextButton.setFont(new Font("Arial", Font.BOLD, 12));
        Color blue = new Color(13, 101, 217);
        nextButton.setBackground(blue);
        nextButton.setForeground(Color.WHITE);
		nextButton.setPreferredSize(new Dimension(80, 25));
		
		// Create previous button
		prevButton = new JButton("Back");
		prevButton.setFont(new Font("Arial", Font.BOLD, 12));
		Color grey = new Color(99, 99, 99);
		prevButton.setBackground(grey);
		prevButton.setForeground(Color.WHITE);
		prevButton.setPreferredSize(new Dimension(80, 25));
		prevButton.setVisible(false);

		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 gotoPrevPanel();
			}
		});

		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 gotoNextPanel();
			}
		});

		bottomPanel.add(prevButton);
		bottomPanel.add(nextButton);
		frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
	}

	private void gotoPrevPanel() {
		// remove current panel
		frame.remove(scrollPane);
		panelCounter--;

		if (panelCounter == 0) {
			prevButton.setVisible(false);
		}

		if (panelCounter == panels.size()-2) {
			nextButton.setVisible(true);
			submitButton.setVisible(false);
		}
		
		// add the previous panel
		scrollPane = new JScrollPane(panels.get(panelCounter));
	   	frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
	   	frame.revalidate();
	}
    
    private void gotoNextPanel() {
		// remove current panel
		frame.remove(scrollPane);
    	panelCounter++;

		// create previous button
		if (panelCounter == 1) {
			prevButton.setVisible(true);
		}

		// add submit button
		if (panelCounter == panels.size()-1) {
			nextButton.setVisible(false);
			submitButton.setVisible(true);
		}

		// add the next panel
    	scrollPane = new JScrollPane(panels.get(panelCounter));
	   	frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
	   	frame.revalidate();
    }

    public JFrame createUI() {
    	ArrayList<String> config = getConfig(filePath);
    	createPanels(config);
    	
        // create frame
        frame.getContentPane().setLayout(new BorderLayout());

        // title panel
        JPanel top = new JPanel();
		top.setBackground(new Color(13, 101, 217));
        JLabel label = new JLabel("General Medical Survey");
        label.setFont(new Font("Arial", Font.BOLD, 20));
		label.setForeground(new Color(255, 255, 255));
        top.add(label);
        frame.getContentPane().add(top, BorderLayout.NORTH);

		// add the first panel
		scrollPane = new JScrollPane(panels.get(0));
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		// add bottom panel
		createButtomPanel();
        return frame;
    }
}