package ru.kamaz.bbp.forms.cases;

import java.util.HashMap;
import java.util.Map;

//����� ��� ��������� ���������������� ����� ����� � ������ ������ ����
public class L_TextPDF {
	private String valueString;
	private int lengthString;
	
	private char valueChar;
	private int lengthChar;
	
	//����������� �� ���������
	public L_TextPDF() {}
	
	//����������� ��� ������������� ������
	public L_TextPDF(String valueString) {
		this.valueString = valueString;
		lengthString = mySizeString(valueString);
	}
	
	//����������� ��� ������������� �������
	public L_TextPDF(char valueChar) {
		this.valueChar = valueChar;
		lengthChar = mySizeSimvole(valueChar);
	}
	
	public String getValueString() {
		return valueString;
	}

	public void setValueString(String valueString) {
		this.valueString = valueString;
		lengthString = mySizeString(valueString);
	}

	public char getValueChar() {
		return valueChar;
	}

	public void setValueChar(char valueChar) {
		this.valueChar = valueChar;
		lengthChar = mySizeSimvole(valueChar);
	}
	
	//������� ����������� Rows ��������� ������� ���� �� ��������
	public int setNumberRowsIgnoreSpaces(int lengthOneRow,String valueString) {
	    return (int) Math.ceil((double) valueString.length() / lengthOneRow);
	}
	
	//���������� ��� �������� �������
	public int setNumberRowsIgnoreSpaces(int lengthOneRow) {
		   return (int) Math.ceil((double) lengthString / lengthOneRow);
	}
	
	//���������� ��� ���������� ������������ ������ ������ � ����� ���������� ���� ��� ���������� �� ������
	public int setNumberRowsIgnoreSpaces(int lengthOneRow,String[] valueStringMas) {
		String maxValueString="";
		for (String valueString: valueStringMas) {
			if (valueString.length()>maxValueString.length()) { maxValueString = valueString;}
		}
		return setNumberRowsIgnoreSpaces(lengthOneRow, maxValueString);
	}
	
	//���������� ��� ���������� ������������ ������ ������ � ����� ���������� ������ �� ������
	public int setNumberRowsIgnoreSpaces(int[] lengthStringMas, String[] valueStringMas) {
		if (lengthStringMas==null||valueStringMas==null) {return 1;}
		resizeLengthStringMas(lengthStringMas,valueStringMas);
		int maxLengthString = 0;
		for (int i=0;i<valueStringMas.length;i++) {
			int lengthString = setNumberRowsIgnoreSpaces(lengthStringMas[i],valueStringMas[i]);
			if (lengthString>maxLengthString) { maxLengthString = lengthString;}
		}
		return maxLengthString;
	}
	
	//������� ����������� Rows � ������ �������� 
	public int setNumberRows(int lengthOneRow, String valueString) {
		if (valueString == null) {return 1; }
	    int currentLength = 0; // ������� ����� ������
	    int numberOfRows = 1; // �������� � ������ ������
	    String[] words = valueString.split(" "); // ��������� ������ �� �����

	    for (String word : words) {
	        // ���������, ���� ����� ������� ����� ������
	        while (mySizeString(word) > lengthOneRow) {
	            // ��������� ����� �� ����� ������ lengthOneRow
	            String part = word.substring(0, limitIndex(lengthOneRow,word));
	            numberOfRows++; // ����������� ���������� �����
	            currentLength = 0; // ��������� ����� ����� � ������
	            word = word.substring(limitIndex(lengthOneRow,word)); // ��������� ������� �����
	        }

	        int wordLength = mySizeString(word);

	        // ���� ������� ����� ���������� � ������
	        if (currentLength + wordLength <= lengthOneRow) {
	            currentLength += wordLength + 1; // ��������� ������ ����� �����
	        } else {
	            // ���� ����� �� ����������, �������� ����� ������
	            numberOfRows++;
	            currentLength = wordLength + 35; // ��������� ������ ����� ������ �����
	        }
	    }

	    return numberOfRows;
	}

	
	//���������� ��� �������� �������
	public int setNumberRows(int lengthOneRow) {
		return setNumberRows(lengthOneRow,valueString);
	}
	
	//���������� ��� ���������� ������������ ������ ������ � ����� ���������� ���� ��� ���������� �� ������
	public int setNumberRows(int lengthOneRow,String[] valueStringMas) {
		String maxValueString="";
		for (String valueString: valueStringMas) {
			if (valueString.length()>maxValueString.length()) { maxValueString = valueString;}
		}
		return setNumberRows(lengthOneRow, maxValueString);
	}
	
	//���������� ��� ���������� ������������ ������ ������ � ����� ���������� ������ �� ������
	public int setNumberRows(int[] lengthStringMas, String[] valueStringMas) {
		if (lengthStringMas==null||valueStringMas==null) {return 1;}
		resizeLengthStringMas(lengthStringMas,valueStringMas);
		int maxLengthString = 0;
		for (int i=0;i<valueStringMas.length;i++) {
			int lengthString = setNumberRows(lengthStringMas[i],valueStringMas[i]);
			if (lengthString>maxLengthString) { maxLengthString = lengthString;}
		}
		return maxLengthString;
	}
	
	private void resizeLengthStringMas(int[] lengthStringMas, String[] valueStringMas) {
	    // ���������, ����� �� �������� ������ �������
	    if (lengthStringMas.length < valueStringMas.length) {
	        // ������� ����� ������ � �������� valueStringMas.length
	        int[] newLengthStringMas = new int[valueStringMas.length];

	        // �������� �������� �� lengthStringMas � ����� ������
	        for (int i = 0; i < lengthStringMas.length; i++) {
	            newLengthStringMas[i] = lengthStringMas[i];
	        }

	        // ��������� ��������� ������� lengthStringMas
	        if (lengthStringMas.length > 0) {
	            int lastElement = lengthStringMas[lengthStringMas.length - 1];
	            for (int i = lengthStringMas.length; i < newLengthStringMas.length; i++) {
	                newLengthStringMas[i] = lastElement;
	            }
	        }

	        lengthStringMas = newLengthStringMas;
	    }
	}
	
	public int mySizeSimvole(char c) {
		Map<Character, Integer> sizeSim = new HashMap<Character, Integer>();

    // ��� ������� ����������
	sizeSim.put(' ', 35);sizeSim.put('q', 62);sizeSim.put('w', 82);sizeSim.put('e', 60);sizeSim.put('r', 43);sizeSim.put('t', 40);sizeSim.put('y', 60);sizeSim.put('u', 63);sizeSim.put('i', 27);sizeSim.put('o', 61);sizeSim.put('p', 63);sizeSim.put('[', 45);sizeSim.put(']', 45);
	sizeSim.put('a', 60);sizeSim.put('s', 52);sizeSim.put('d', 62);sizeSim.put('f', 35);sizeSim.put('g', 62);sizeSim.put('h', 63);sizeSim.put('j', 34);sizeSim.put('k', 59);sizeSim.put('l', 27);sizeSim.put(';', 45);sizeSim.put('\'', 27);sizeSim.put('\\', 45);
	sizeSim.put('z', 53);sizeSim.put('x', 59);sizeSim.put('c', 52);sizeSim.put('v', 59);sizeSim.put('b', 62);sizeSim.put('n', 63);sizeSim.put('m', 97);sizeSim.put(',', 36);sizeSim.put('.', 36);sizeSim.put('/', 45);sizeSim.put('`', 64);sizeSim.put('1', 64);
	sizeSim.put('2', 64);sizeSim.put('3', 64);sizeSim.put('4', 64);sizeSim.put('5', 64);sizeSim.put('6', 64);sizeSim.put('7', 64);sizeSim.put('8', 64);sizeSim.put('9', 64);sizeSim.put('0', 64);sizeSim.put('-', 45);/*sizeSim.put('=', 82);*/sizeSim.put('*', 64);
	sizeSim.put('_', 64);sizeSim.put('+', 82);sizeSim.put('Q', 79);sizeSim.put('W', 99);sizeSim.put('E', 63);sizeSim.put('R', 70);sizeSim.put('T', 62);sizeSim.put('Y', 62);sizeSim.put('U', 73);sizeSim.put('I', 42);sizeSim.put('O', 79);sizeSim.put('P', 61);
	sizeSim.put('A', 69);sizeSim.put('S', 68);sizeSim.put('D', 77);sizeSim.put('F', 57);sizeSim.put('G', 78);sizeSim.put('H', 75);sizeSim.put('J', 45);sizeSim.put('K', 69);sizeSim.put('L', 56);sizeSim.put('Z', 69);sizeSim.put('X', 69);sizeSim.put('C', 70);
	sizeSim.put('V', 69);sizeSim.put('B', 69);sizeSim.put('N', 75);sizeSim.put('M', 84);sizeSim.put('~', 82);sizeSim.put('{', 64);sizeSim.put('}', 64);sizeSim.put(':', 46);sizeSim.put('"', 46);sizeSim.put('|', 46);sizeSim.put('<', 82);sizeSim.put('>', 82);
	sizeSim.put('?', 55);sizeSim.put('�', 64);sizeSim.put('�', 65);sizeSim.put('�', 59);sizeSim.put('�', 59);sizeSim.put('�', 60);sizeSim.put('�', 64);sizeSim.put('�', 47);sizeSim.put('�', 88);sizeSim.put('�', 89);sizeSim.put('�', 53);sizeSim.put('�', 59);
	sizeSim.put('�', 64);sizeSim.put('�', 84);sizeSim.put('�', 80);sizeSim.put('�', 60);sizeSim.put('�', 60);sizeSim.put('�', 64);sizeSim.put('�', 63);sizeSim.put('�', 61);sizeSim.put('�', 62);sizeSim.put('�', 62);sizeSim.put('�', 80);sizeSim.put('�', 55);
	sizeSim.put('�', 60);sizeSim.put('�', 61);sizeSim.put('�', 54);sizeSim.put('�', 70);sizeSim.put('�', 64);sizeSim.put('�', 50);sizeSim.put('�', 57);sizeSim.put('�', 62);sizeSim.put('�', 84);sizeSim.put('�', 60);sizeSim.put('!', 39);sizeSim.put('�', 117);
	sizeSim.put('%', 108);sizeSim.put('(', 46);sizeSim.put(')', 46);sizeSim.put('@', 100);sizeSim.put('#', 82);sizeSim.put('$', 64);sizeSim.put('^', 82);sizeSim.put('&', 73);sizeSim.put('�', 75);sizeSim.put('�', 72);sizeSim.put('�', 62);sizeSim.put('�', 69);
	sizeSim.put('�', 63);sizeSim.put('�', 75);sizeSim.put('�', 57);sizeSim.put('�', 103);sizeSim.put('�', 105);sizeSim.put('�', 62);sizeSim.put('�', 69);sizeSim.put('�', 79);sizeSim.put('�', 82);sizeSim.put('�', 92);sizeSim.put('�', 69);sizeSim.put('�', 68);
	sizeSim.put('�', 75);sizeSim.put('�', 60);sizeSim.put('�', 79);sizeSim.put('�', 75);sizeSim.put('�', 75);sizeSim.put('�', 98);sizeSim.put('�', 70);sizeSim.put('�', 71);sizeSim.put('�', 71);sizeSim.put('�', 70);sizeSim.put('�', 84);sizeSim.put('�', 75);
	sizeSim.put('�', 62);sizeSim.put('�', 68);sizeSim.put('�', 69);sizeSim.put('�', 103);sizeSim.put('�', 63);
	
	int i =0; boolean out=false;
	for (Map.Entry<Character, Integer> entry : sizeSim.entrySet()) {
	    Character innerKey = entry.getKey();
	    if (innerKey==c) {
	    	i = entry.getValue();
	    	out=true;
	    	break;
	    }
	}

	//���� �������
	if (!out) {
		String s = ""+c;
		if (s.indexOf(String.valueOf((char) 9711))>=0) {i=150;} //����+++
		else if (s.indexOf(String.valueOf((char) 8212))>=0) {i=150;} //����� ��������������+++
		else if (s.indexOf(String.valueOf((char) 9649))>=0) {i=180;} //��������������+++
		else if (s.indexOf(String.valueOf((char) 9005))>=0) {i=160;} //���� ����� ������� ��� ��������+++
		else if (s.indexOf(String.valueOf((char) 61))>=0) {i=100;} //�����+++
		else if (s.indexOf(String.valueOf((char) 8725))>=0) {i=90;} //��������� ��� �����+++
		else if (s.indexOf(String.valueOf((char) 9162))>=0) {i=150;} //������������������+++
		else if (s.indexOf(String.valueOf((char) 10655))>=0) {i=150;} //����+++
		else if (s.indexOf(String.valueOf((char) 9022))>=0) {i=150;} //���� � �����+++
		else if (s.indexOf(String.valueOf((char) 9007))>=0) {i=150;} //������+++
		else if (s.indexOf(String.valueOf((char) 8982))>=0) {i=150;} //������+++
		else if (s.indexOf(String.valueOf((char) 10005))>=0) {i=150;} //�����������
		else if (s.indexOf(String.valueOf((char) 10138))>=0) {i=150;} //�������+++
		else if (s.indexOf(String.valueOf((char) 9008))>=0) {i=150;} //��� ������� ������+++
		else if (s.indexOf(String.valueOf((char) 8978))>=0) {i=180;} //����+++
		else if (s.indexOf(String.valueOf((char) 8979))>=0) {i=170;} //��������������+++//8241
		else if (s.indexOf(String.valueOf((char) 8241))>=0) {i=210;} //%��
		else {i=90;}
	}
	return i;
	}
    //����� ������� ��������� ���������� ����� ��������� ��������� ����� ������, �������� ������ ������
	public int mySizeString(String str) {
		if (str==null|| str.length()==0) return 0;
		int length=0;
		for (int i=0; i<str.length();i++) {
			char c = str.charAt(i);
			length+=mySizeSimvole(c);
		}
		return length;
	}
	
	private int limitIndex(int length, String word) {
		if (word==null||word.length()==0) return -1;
		int current = 0;
		int count = 0;
		for (int i=0;i<word.length()&&current<length;i++) {
			current+=mySizeSimvole(word.charAt(i));
			count = i;
		}
		return count;
	}
}
