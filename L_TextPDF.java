package ru.kamaz.bbp.forms.cases;

import java.util.HashMap;
import java.util.Map;

//Класс для получения пропорциональной длины строк с учетом ширины букв
public class L_TextPDF {
	private String valueString;
	private int lengthString;
	
	private char valueChar;
	private int lengthChar;
	
	//Конструктор по умолчанию
	public L_TextPDF() {}
	
	//Конструктор для инициализации строки
	public L_TextPDF(String valueString) {
		this.valueString = valueString;
		lengthString = mySizeString(valueString);
	}
	
	//Конструктор для инициализации символа
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
	
	//Подсчет колличество Rows игнорируя перенос слов по пробелам
	public int setNumberRowsIgnoreSpaces(int lengthOneRow,String valueString) {
	    return (int) Math.ceil((double) valueString.length() / lengthOneRow);
	}
	
	//Перегрузка для значения объекта
	public int setNumberRowsIgnoreSpaces(int lengthOneRow) {
		   return (int) Math.ceil((double) lengthString / lengthOneRow);
	}
	
	//Перегрузка для вычисления максимальной высоты строки и среди нескольких если все одинаковые по ширине
	public int setNumberRowsIgnoreSpaces(int lengthOneRow,String[] valueStringMas) {
		String maxValueString="";
		for (String valueString: valueStringMas) {
			if (valueString.length()>maxValueString.length()) { maxValueString = valueString;}
		}
		return setNumberRowsIgnoreSpaces(lengthOneRow, maxValueString);
	}
	
	//Перегрузка для вычисления максимальной высоты строки и среди нескольких разных по ширине
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
	
	//Подсчет колличество Rows с учетом пробелов 
	public int setNumberRows(int lengthOneRow, String valueString) {
		if (valueString == null) {return 1; }
	    int currentLength = 0; // Текущая длина строки
	    int numberOfRows = 1; // Начинаем с первой строки
	    String[] words = valueString.split(" "); // Разделяем строку на слова

	    for (String word : words) {
	        // Проверяем, если слово длиннее длины строки
	        while (mySizeString(word) > lengthOneRow) {
	            // Разбиваем слово на части длиной lengthOneRow
	            String part = word.substring(0, limitIndex(lengthOneRow,word));
	            numberOfRows++; // Увеличиваем количество строк
	            currentLength = 0; // Учитываем длину части и пробел
	            word = word.substring(limitIndex(lengthOneRow,word)); // Оставляем остаток слова
	        }

	        int wordLength = mySizeString(word);

	        // Если текущее слово помещается в строку
	        if (currentLength + wordLength <= lengthOneRow) {
	            currentLength += wordLength + 1; // Учитываем пробел после слова
	        } else {
	            // Если слово не помещается, начинаем новую строку
	            numberOfRows++;
	            currentLength = wordLength + 35; // Учитываем пробел после нового слова
	        }
	    }

	    return numberOfRows;
	}

	
	//Перегрузка для значения объекта
	public int setNumberRows(int lengthOneRow) {
		return setNumberRows(lengthOneRow,valueString);
	}
	
	//Перегрузка для вычисления максимальной высоты строки и среди нескольких если все одинаковые по ширине
	public int setNumberRows(int lengthOneRow,String[] valueStringMas) {
		String maxValueString="";
		for (String valueString: valueStringMas) {
			if (valueString.length()>maxValueString.length()) { maxValueString = valueString;}
		}
		return setNumberRows(lengthOneRow, maxValueString);
	}
	
	//Перегрузка для вычисления максимальной высоты строки и среди нескольких разных по ширине
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
	    // Проверяем, нужно ли изменять размер массива
	    if (lengthStringMas.length < valueStringMas.length) {
	        // Создаем новый массив с размером valueStringMas.length
	        int[] newLengthStringMas = new int[valueStringMas.length];

	        // Копируем элементы из lengthStringMas в новый массив
	        for (int i = 0; i < lengthStringMas.length; i++) {
	            newLengthStringMas[i] = lengthStringMas[i];
	        }

	        // Дублируем последний элемент lengthStringMas
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

    // все символы клавиатуры
	sizeSim.put(' ', 35);sizeSim.put('q', 62);sizeSim.put('w', 82);sizeSim.put('e', 60);sizeSim.put('r', 43);sizeSim.put('t', 40);sizeSim.put('y', 60);sizeSim.put('u', 63);sizeSim.put('i', 27);sizeSim.put('o', 61);sizeSim.put('p', 63);sizeSim.put('[', 45);sizeSim.put(']', 45);
	sizeSim.put('a', 60);sizeSim.put('s', 52);sizeSim.put('d', 62);sizeSim.put('f', 35);sizeSim.put('g', 62);sizeSim.put('h', 63);sizeSim.put('j', 34);sizeSim.put('k', 59);sizeSim.put('l', 27);sizeSim.put(';', 45);sizeSim.put('\'', 27);sizeSim.put('\\', 45);
	sizeSim.put('z', 53);sizeSim.put('x', 59);sizeSim.put('c', 52);sizeSim.put('v', 59);sizeSim.put('b', 62);sizeSim.put('n', 63);sizeSim.put('m', 97);sizeSim.put(',', 36);sizeSim.put('.', 36);sizeSim.put('/', 45);sizeSim.put('`', 64);sizeSim.put('1', 64);
	sizeSim.put('2', 64);sizeSim.put('3', 64);sizeSim.put('4', 64);sizeSim.put('5', 64);sizeSim.put('6', 64);sizeSim.put('7', 64);sizeSim.put('8', 64);sizeSim.put('9', 64);sizeSim.put('0', 64);sizeSim.put('-', 45);/*sizeSim.put('=', 82);*/sizeSim.put('*', 64);
	sizeSim.put('_', 64);sizeSim.put('+', 82);sizeSim.put('Q', 79);sizeSim.put('W', 99);sizeSim.put('E', 63);sizeSim.put('R', 70);sizeSim.put('T', 62);sizeSim.put('Y', 62);sizeSim.put('U', 73);sizeSim.put('I', 42);sizeSim.put('O', 79);sizeSim.put('P', 61);
	sizeSim.put('A', 69);sizeSim.put('S', 68);sizeSim.put('D', 77);sizeSim.put('F', 57);sizeSim.put('G', 78);sizeSim.put('H', 75);sizeSim.put('J', 45);sizeSim.put('K', 69);sizeSim.put('L', 56);sizeSim.put('Z', 69);sizeSim.put('X', 69);sizeSim.put('C', 70);
	sizeSim.put('V', 69);sizeSim.put('B', 69);sizeSim.put('N', 75);sizeSim.put('M', 84);sizeSim.put('~', 82);sizeSim.put('{', 64);sizeSim.put('}', 64);sizeSim.put(':', 46);sizeSim.put('"', 46);sizeSim.put('|', 46);sizeSim.put('<', 82);sizeSim.put('>', 82);
	sizeSim.put('?', 55);sizeSim.put('й', 64);sizeSim.put('ц', 65);sizeSim.put('у', 59);sizeSim.put('к', 59);sizeSim.put('е', 60);sizeSim.put('н', 64);sizeSim.put('г', 47);sizeSim.put('ш', 88);sizeSim.put('щ', 89);sizeSim.put('з', 53);sizeSim.put('х', 59);
	sizeSim.put('ъ', 64);sizeSim.put('ф', 84);sizeSim.put('ы', 80);sizeSim.put('в', 60);sizeSim.put('а', 60);sizeSim.put('п', 64);sizeSim.put('р', 63);sizeSim.put('о', 61);sizeSim.put('л', 62);sizeSim.put('д', 62);sizeSim.put('ж', 80);sizeSim.put('э', 55);
	sizeSim.put('я', 60);sizeSim.put('ч', 61);sizeSim.put('с', 54);sizeSim.put('м', 70);sizeSim.put('и', 64);sizeSim.put('т', 50);sizeSim.put('ь', 57);sizeSim.put('б', 62);sizeSim.put('ю', 84);sizeSim.put('ё', 60);sizeSim.put('!', 39);sizeSim.put('№', 117);
	sizeSim.put('%', 108);sizeSim.put('(', 46);sizeSim.put(')', 46);sizeSim.put('@', 100);sizeSim.put('#', 82);sizeSim.put('$', 64);sizeSim.put('^', 82);sizeSim.put('&', 73);sizeSim.put('Й', 75);sizeSim.put('Ц', 72);sizeSim.put('У', 62);sizeSim.put('К', 69);
	sizeSim.put('Е', 63);sizeSim.put('Н', 75);sizeSim.put('Г', 57);sizeSim.put('Ш', 103);sizeSim.put('Щ', 105);sizeSim.put('З', 62);sizeSim.put('Х', 69);sizeSim.put('Ъ', 79);sizeSim.put('Ф', 82);sizeSim.put('Ы', 92);sizeSim.put('В', 69);sizeSim.put('А', 68);
	sizeSim.put('П', 75);sizeSim.put('Р', 60);sizeSim.put('О', 79);sizeSim.put('Л', 75);sizeSim.put('Д', 75);sizeSim.put('Ж', 98);sizeSim.put('Э', 70);sizeSim.put('Я', 71);sizeSim.put('Ч', 71);sizeSim.put('С', 70);sizeSim.put('М', 84);sizeSim.put('И', 75);
	sizeSim.put('Т', 62);sizeSim.put('Ь', 68);sizeSim.put('Б', 69);sizeSim.put('Ю', 103);sizeSim.put('Ё', 63);
	
	int i =0; boolean out=false;
	for (Map.Entry<Character, Integer> entry : sizeSim.entrySet()) {
	    Character innerKey = entry.getKey();
	    if (innerKey==c) {
	    	i = entry.getValue();
	    	out=true;
	    	break;
	    }
	}

	//спец символы
	if (!out) {
		String s = ""+c;
		if (s.indexOf(String.valueOf((char) 9711))>=0) {i=150;} //круг+++
		else if (s.indexOf(String.valueOf((char) 8212))>=0) {i=150;} //линия горизонтальная+++
		else if (s.indexOf(String.valueOf((char) 9649))>=0) {i=180;} //параллелограмм+++
		else if (s.indexOf(String.valueOf((char) 9005))>=0) {i=160;} //круг между линиями под наклоном+++
		else if (s.indexOf(String.valueOf((char) 61))>=0) {i=100;} //равно+++
		else if (s.indexOf(String.valueOf((char) 8725))>=0) {i=90;} //наклонные две линии+++
		else if (s.indexOf(String.valueOf((char) 9162))>=0) {i=150;} //перпендикулярность+++
		else if (s.indexOf(String.valueOf((char) 10655))>=0) {i=150;} //угол+++
		else if (s.indexOf(String.valueOf((char) 9022))>=0) {i=150;} //круг в круге+++
		else if (s.indexOf(String.valueOf((char) 9007))>=0) {i=150;} //кнопка+++
		else if (s.indexOf(String.valueOf((char) 8982))>=0) {i=150;} //прицел+++
		else if (s.indexOf(String.valueOf((char) 10005))>=0) {i=150;} //пересечение
		else if (s.indexOf(String.valueOf((char) 10138))>=0) {i=150;} //стрелка+++
		else if (s.indexOf(String.valueOf((char) 9008))>=0) {i=150;} //две стрелки наклон+++
		else if (s.indexOf(String.valueOf((char) 8978))>=0) {i=180;} //дуга+++
		else if (s.indexOf(String.valueOf((char) 8979))>=0) {i=170;} //полуокружность+++//8241
		else if (s.indexOf(String.valueOf((char) 8241))>=0) {i=210;} //%оо
		else {i=90;}
	}
	return i;
	}
    //метод который используя предыдущий метод вычисляет суммарную длину строки, вычеслив каждый символ
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
