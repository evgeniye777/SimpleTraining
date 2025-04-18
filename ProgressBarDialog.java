package com.kamaz.technology.apps.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.teamcenter.rac.aif.AbstractAIFDialog;

public class ProgressBarDialog extends AbstractAIFDialog {

	private static final long serialVersionUID = 1L;
	JProgressBar progressBar;
	static boolean stop = false;
	JButton b;
    JLabel lab = new JLabel("Подготовка процесса...",JLabel.CENTER);
	public ProgressBarDialog(AbstractAIFDialog dialog, String title, int width, int height) {
		super(dialog, title);

		Font f = new Font("TimesRoman", Font.BOLD, 15);
		lab.setFont(f);
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		lab.setVisible(false);
		
		b = new JButton ("Прервать");
        b.addActionListener ( new ActionListener()  
        {  
            public void actionPerformed( ActionEvent e )  
            {  
            	progressBar.setIndeterminate(true);
            	ProgressBarDialog.stop=true;  
            }  
        });  
        b.setLocation(width/2-30, 2);
        b.setSize(50, 30);
        Insets m = new Insets(0, 50, 0, 50);
        b.setMargin(m);
        b.setForeground(Color.BLACK);
        b.setBackground(Color.WHITE);
        Border line = new LineBorder(Color.BLACK);
        Border margin = new EmptyBorder(5, 15, 5, 15);
        Border compound = new CompoundBorder(line, margin);
        b.setBorder(compound);
        b.setVisible(false);

        JPanel centerBottom = new JPanel ();
        centerBottom.setSize(width, 55);
        centerBottom.add(b);
        centerBottom.setVisible(true);
		add(progressBar,"North");
		add(lab);
		add(centerBottom,"South");
		
		setPreferredSize(new Dimension(width, height));
		pack();
		setAlwaysOnTop(true);
		setLocationRelativeTo(dialog);
		
		progressBar.setMinimum(0);
	}
	public void setMax(int max) {lab.setVisible(true);progressBar.setMaximum(max);}
	public void setInterminate() {progressBar.setIndeterminate(false);}
	public void setValue(int value) {progressBar.setValue(value);}
	public void setLabel(String value) {lab.setText(value);}
	public void setButtonStop(boolean value) {b.setVisible(value); stop = false;}
	public boolean getStatusStop() {return stop;}
}