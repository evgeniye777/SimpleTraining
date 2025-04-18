package com.kamaz.technology.apps.dialogs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.Font;



public class InputAllDate implements DocumentListener {
    private static JDialog d; 
    boolean clous = false;
    JTextField text0,text1,text2,text3,text4,text5,text5_1,text5_2,text5_3,text5_4,text6,text6_1,text6_2,text6_3,text6_4;
    JLabel lab0,lab1,lab2,lab3,lab4,lab5,lab6,lab7,lab8;
    String[] dateTLocal;
   public InputAllDate() {  
        JFrame f= new JFrame();  
        dateTLocal = new String[33];
        f.setLayout(null);
        
        d = new JDialog(f , "Введите...", true); 
        d.addWindowListener(new WindowListener() {
            public void windowActivated(WindowEvent event) {}
            public void windowClosed(WindowEvent event) {}
            public void windowClosing(WindowEvent event) {
                clous=true;
            }
            public void windowDeactivated(WindowEvent event) { }
            public void windowDeiconified(WindowEvent event) {}
            public void windowIconified(WindowEvent event) {}
            public void windowOpened(WindowEvent event) {}
        });
        
        text0 = textCreate(0);
        text1 = textCreate(1);
        text2 = textCreate(2);
        text3 = textCreate(3);
        text4 = textCreate(4);
        text5 = textCreate(0,5); text5_1 = textCreate(1,5); text5_2 = textCreate(2,5); text5_3 = textCreate(3,5); text5_4 = textCreate(4,5);
        text6 = textCreate(0,6); text6_1 = textCreate(1,6); text6_2 = textCreate(2,6); text6_3 = textCreate(3,6); text6_4 = textCreate(4,6);
	    text5.getDocument().addDocumentListener(this);
	    text5_1.getDocument().addDocumentListener(this);
	    text5_2.getDocument().addDocumentListener(this);
	    text5_3.getDocument().addDocumentListener(this);
	    text5_4.getDocument().addDocumentListener(this);
	    
	    text6.getDocument().addDocumentListener(this);
	    text6_1.getDocument().addDocumentListener(this);
	    text6_2.getDocument().addDocumentListener(this);
	    text6_3.getDocument().addDocumentListener(this);
	    text6_4.getDocument().addDocumentListener(this);
        
        JButton b = new JButton ("OK");
        b.addActionListener ( new ActionListener()  
        {  
            public void actionPerformed( ActionEvent e )  
            {  
            	InputAllDate.d.setVisible(false);  
            }  
        });  
        b.setLocation(195, 400);
        b.setSize(50, 30);
        b.setForeground(Color.BLACK);
        b.setBackground(Color.WHITE);
        Border line = new LineBorder(Color.BLACK);
        Border margin = new EmptyBorder(5, 15, 5, 15);
        Border compound = new CompoundBorder(line, margin);
        b.setBorder(compound);
        
        lab0 = labelCreate(0,"Введите шифр производственных затрат");
        lab1 = labelCreate(1,"Введите вид заготовки");
        lab2 = labelCreate(2,"Введите профиль и размеры");
        lab3 = labelCreate(3,"Разработал технологический процесс, ФИО");
        lab4 = labelCreate(4,"Проверил технологический процесс, ФИО");
        lab5 = labelCreate(5,"Введите номер технологического процесса");
        lab6 = labelCreate(6,"Введите номер карты наладки");
        lab7 = labelCreate2(5,"37.104.17.10142.");
        lab8 = labelCreate2(6,"КН 37.104.17.62142.");
        
        JPanel p= new JPanel();  
        p.setLayout(null);
        p.setBackground(new Color(255, 228, 196));
        p.add(lab0); p.add(text0);
        p.add(lab1); p.add(text1);
        p.add(lab2); p.add(text2);
        p.add(lab3); p.add(text3);
        p.add(lab4); p.add(text4);
        p.add(lab5); p.add(text5);
        p.add(lab6); p.add(text6);
        p.add(lab7); p.add(text5_1);p.add(text5_2);p.add(text5_3);p.add(text5_4);
        p.add(lab8); p.add(text6_1);p.add(text6_2);p.add(text6_3);p.add(text6_4);
        p.add(b);  
        d.add(p);
        d.setSize(450,480);  
        d.setLocationRelativeTo(null);
        d.setVisible(true);  
    } 
   public String getLab0() {return text0.getText();}
   public String getLab1() {return text1.getText();}
   public String getLab2() {return text2.getText();}
   public String getLab3() {return text3.getText();}
   public String getLab4() {return text4.getText();}
   public String getLab5() {return lab7.getText()+text5.getText()+text5_1.getText()+text5_2.getText()+text5_3.getText()+text5_4.getText();}
   public String getLab6() {return lab8.getText()+text6.getText()+text6_1.getText()+text6_2.getText()+text6_3.getText()+text6_4.getText();}
   
   public boolean getClosing() {return clous;}
   
   private JTextField textCreate(int n) {
	   JTextField Pole;
	   Pole = new JTextField();
	   Pole.setLocation(50, 55+50*n);
	   Pole.setSize(350, 25);
       return Pole;
   }
   private JTextField textCreate(int x,int y) {
	   JTextField Pole;
	   Pole = new JTextField();
	   int yS=170;
	   if (y==6) {yS=190;}
	   Pole.setLocation(yS+20*x, 57+50*y);
	   Pole.setSize(17, 17);
       return Pole;
   }
   private JLabel labelCreate(int n,String lab) {
	   JLabel j = new JLabel (lab);
       j.setFont(new Font("Verdana", Font.PLAIN, 13));
       j.setLocation(50, 30+50*n);
       j.setSize(350, 20);
       return j;
   }
   private JLabel labelCreate2(int n,String lab) {
	   JLabel j = new JLabel (lab);
       j.setFont(new Font("Verdana", Font.PLAIN, 13));
       j.setLocation(50, 55+50*n);
       j.setSize(350, 20);
       return j;
   }
@Override
public void changedUpdate(DocumentEvent e) {
}
@Override
public void insertUpdate(DocumentEvent e) {
	if (e.getDocument() == text5.getDocument()) {text5_1.requestFocus();}
	else if (e.getDocument() == text5_1.getDocument()) {text5_2.requestFocus();}
	else if (e.getDocument() == text5_2.getDocument()) {text5_3.requestFocus();}
	else if (e.getDocument() == text5_3.getDocument()) {text5_4.requestFocus();}
	else if (e.getDocument() == text5_4.getDocument()) {text6.requestFocus();}
	else if (e.getDocument() == text6.getDocument()) {text6_1.requestFocus();}
	else if (e.getDocument() == text6_1.getDocument()) {text6_2.requestFocus();}
	else if (e.getDocument() == text6_2.getDocument()) {text6_3.requestFocus();}
	else if (e.getDocument() == text6_3.getDocument()) {text6_4.requestFocus();}
}
@Override
public void removeUpdate(DocumentEvent e) {
    if (e.getDocument() == text5_1.getDocument()) {text5.requestFocus();}
	else if (e.getDocument() == text5_2.getDocument()) {text5_1.requestFocus();}
	else if (e.getDocument() == text5_3.getDocument()) {text5_2.requestFocus();}
	else if (e.getDocument() == text5_4.getDocument()) {text5_3.requestFocus();}
	else if (e.getDocument() == text6.getDocument()) {text5_4.requestFocus();}
	else if (e.getDocument() == text6_1.getDocument()) {text6.requestFocus();}
	else if (e.getDocument() == text6_2.getDocument()) {text6_1.requestFocus();}
	else if (e.getDocument() == text6_3.getDocument()) {text6_2.requestFocus();}
	else if (e.getDocument() == text6_4.getDocument()) {text6_3.requestFocus();}
}

}
