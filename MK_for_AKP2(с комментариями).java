package com.kamaz.technology.apps.cust;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;


import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
//import java.awt.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.kamaz.technology.apps.dialogs.InputAllDate;
import com.kamaz.technology.apps.dialogs.MTDDialog;
import com.kamaz.technology.apps.dialogs.MessageDialog;
import com.kamaz.technology.apps.dialogs.ProgressBarDialog;
import com.kamaz.technology.apps.utils.HTML2Text;
import com.kamaz.technology.apps.utils.Util;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

//Класс для выгрузки данных с TeamCenter и генерации отчёта
public class MK_for_AKP2 {

	String[] dateT;

	List<Operation> listOper = new ArrayList<>();

	private AbstractAIFDialog mainReportDialog;
	private TCComponent tCComponent, tCComponent2;

	private String nameDocument;
	private MTDDialog mtdDialog;
	private TCSession session;
	TCComponentBOMLine bomLine;

     //конструктор класса паредача (формы диалогового окна, выбранного объекта, данных сессии,)
	public MK_for_AKP2(MTDDialog mtdDialog, TCComponent component, TCSession session) throws TCException, DocumentException, IOException {

		this.mtdDialog = mtdDialog;
		bomLine = (TCComponentBOMLine) component;
		this.session = session;
		dateT = new String[34];
		InputAllDate d = new InputAllDate();
		// this.mtdDialog = mtdDialog;
		tCComponent = component;

        //Проверка, не нажата ли кнопка закрыть
		if (d.getClosing()) {
			return; //выход из процесса
		} else {
            //Выгрузка имени объекта и всех необходимых по ТЗ свойств
			try {

				String typeNameAKPP = tCComponent.getProperty("bl_item_object_type");
				InformTarget inf = new InformTarget();
				if (typeNameAKPP.equals("Технологический маршрут")) {
					rekPoiskTarget(component, inf);
					tCComponent = inf.getTm();
				} else {
					tCComponent2 = tCComponent;
					int i=0;
					while (!typeNameAKPP.equals("Технологический маршрут")) {
						bomLine = bomLine.parent();
						tCComponent = bomLine;
						typeNameAKPP = tCComponent.getProperty("bl_item_object_type");
						i++;
						if (i>100) {
							new MessageDialog("Ошибка", "Не удалось найти МК среди родитеских компонентов", 380, 85, true);
							return;}
					}
				}
				AIFComponentContext[] children = tCComponent.getChildren();

				for (int i = 0; i < children.length; i++) {
					AIFComponentContext child = children[i];

					TCComponent target = (TCComponent) child.getComponent();

					String occType = target.getProperty("bl_occ_type"), occName = target.getProperty("bl_item_object_type");

					if (occType.equals("Цель")) {
						dateT[1] = target.getProperty("bl_rev_awp0Item_item_id");

						dateT[2] = target.getProperty("bl_rev_object_name");

						dateT[3] = d.getLab0();

						// Подключение к Вложению
						TCComponentItemRevision itemRevision = ((TCComponentBOMLine) target).getItemRevision();
						TCComponent Master = itemRevision.getRelatedComponent("IMAN_master_form_rev");
						dateT[4] = Master.getProperty("k7_MaterialBasic");
						dateT[5] = "кг";
						dateT[6] = Master.getProperty("Z_MASSA");

						dateT[7] = d.getLab1();
						dateT[8] = d.getLab2();

						dateT[9] = "1";
						dateT[11] = "1";
						dateT[26] = d.getLab3();
						dateT[27] = d.getLab4();
						dateT[32] = d.getLab5();
						dateT[33] = d.getLab6();
					} else if (occName.equals("Технологический процесс") && tCComponent2 == null) {
						tCComponent2 = target;
					}
				}

				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							if (tCComponent2 == null) {
								tCComponent2 = tCComponent;
							}
                            //передача объекта данных для более углубленной записи (проверка вложенных ссылок, выгрузка необходимых свойств по ТЗ)
							listOper = WriteOper(tCComponent2);
						} catch (TCException | IOException e) {
							e.printStackTrace();
						} catch (DocumentException e) {
							e.printStackTrace();
						}
					}
				}).start();

				System.out.print(listOper.size());

			} catch (TCException e) {
				new MessageDialog("Ошибка", "Возможно отсутствуют некоторые данные", 380, 85, true);
			} catch (Exception e) {
				new MessageDialog("Ошибка", "Возможно отсутствуют некоторые данные", 380, 85, true);
			}
			// MessageBox.post(Arrays.toString(dateT), "Message", MessageBox.INFORMATION);

		}
		//tCComponent.clearCache();
	}

    //метод для выгрузки даннных Процесса и переходов, действий
	private List<Operation> WriteOper(TCComponent tcc0) throws TCException, DocumentException, IOException {

		AbstractAIFDialog queryCriteriaDialog = new AbstractAIFDialog(mainReportDialog, "") {
			private static final long serialVersionUID = 1L;
		};
		ProgressBarDialog progressBarDialog = new ProgressBarDialog(queryCriteriaDialog, "Формирование отчёта", 490, 120);
		progressBarDialog.setVisible(true);
		progressBarDialog.setButtonStop(true);
		progressBarDialog.setMax(100);

		List<Operation> listOper = new ArrayList<>();
		nameDocument = tcc0.getProperty("bl_rev_awp0Item_item_id") + "_" + tcc0.getProperty("bl_rev_item_revision_id");
		List<TCComponent> childrenDo = new ArrayList<>();
		ListOperation listOperation = new ListOperation();
		rekPoiskAllOperation(tcc0, listOperation);
		childrenDo = listOperation.getListOper();
		int nTar = childrenDo.size();
		AtomicInteger xI = new AtomicInteger(0);
		progressBarDialog.setMax(nTar);
		progressBarDialog.setInterminate();
		for (int j = 0; j < childrenDo.size(); j++) {
			if (progressBarDialog.getStatusStop()) {
				break;
			}
			Operation oper = new Operation();
			TCComponent target2 = childrenDo.get(j);

			if (!target2.getProperty("bl_item_object_type").equals("Операция")) {
				continue;
			}

			oper.setNumber(target2.getProperty("K7_OpNum"));
			oper.setName(target2.getProperty("bl_rev_object_name"));
			double time = (double) ((double) Math.round(target2.getDoubleProperty("k7_MEOp_piece_time") * 10000) / 10000);
			String timeStr = "";
			if (time > 0) {
				timeStr += time;
			}
			oper.setIntensity(timeStr);

			// Получаем Список Действий (отправить в действие)
			TCComponentCfgActivityLine cfgActivityLine = (TCComponentCfgActivityLine) ((TCComponentBOMLine) target2).getRelatedComponent("bl_me_activity_lines");
			TCComponent[] activityChildLines = cfgActivityLine.getRelatedComponents("me_cl_child_lines");

			List<Revision> kuList = new ArrayList<>();

			for (TCComponent tcT : activityChildLines) {
				Transition tr = new Transition();
				tr.setNumber(tcT.getProperty("al_activity_object_name"));
				String NameP = tcT.getProperty("k7_LongDesciprtion");
				String formatNameP = HTML2Text.convert(NameP);
				NameP = ReplaseФ_D(formatNameP);
				NameP = NameP.replace("\n", " ");
				tr.setName(NameP);
				TCComponent[] masTC = tcT.getRelatedComponents("me_cl_child_lines");// getReferenceListProperty "me_cl_child_lines"
				if (masTC.length > 0) {
					for (TCComponent tcTch : masTC) {
						if (tcTch.getProperty("al_activity_object_name").equals("Режим резания") || tcTch.getProperty("al_activity_object_type").equals("Режим резания")) {
							tr.setMode(tcTch.getProperty("al_activity_long_description"));
						}
					}
				}

				tr.setEqup(tcT.getProperty("al_activity_tool_bl_list"));

				String ku1 = tcT.getProperty("k7_KDDesc"), ku2 = tcT.getProperty("k7_KDExplanation"), ku3 = "" + tcT.getProperty("k7_RefDes");
				if (ku3.length() > 0) {
					ku3 = " (" + ku3 + ")";
				}
				if (ku1 != null && ku2 != null && (ku1.length() + ku2.length()) <= 4340) {
					if ((ku1.length() > 0) && ku1.charAt(ku1.length() - 1) != ' ') {
						ku1 += " ";
					}
					ku1 += ku2 + ku3;
					ku2 = null;
				}
				if (ku1 != null && !ku1.isEmpty()) {
					if (ku2 != null && !ku2.isEmpty()) {
						Revision kuR = new Revision(ku1, ku2 + ku3);
						kuList.add(kuR);
					} else {
						tr.setKU(ku1);
					}
				}

				if (!tr.getNumber().isEmpty() && !tr.getName().isEmpty()) {
					oper.setTran(tr);
				}
			}
			if (oper.getTran() != null && kuList.size() > 0) {
				oper.setKU(kuList);
			}

			AIFComponentContext[] childrenTwoDo = target2.getChildren();

			for (AIFComponentContext tcA : childrenTwoDo) {
				String childA = ((TCComponent) tcA.getComponent()).getProperty("bl_occ_type");
				if (childA.equals("СИЗ")) {
					oper.setSIZ(((TCComponent) tcA.getComponent()).getProperty("bl_rev_object_name"), "0");

				} else if (childA.equals("Вспомогательный инструмент")) {
					TCComponent tc = (TCComponent) tcA.getComponent();
					oper.setVI(tc.getProperty("bl_rev_object_name"), tc.getProperty("bl_rev_awp0Item_item_id"));
				} else if (childA.equals("Режущий инструмент")) {
					TCComponent tc = (TCComponent) tcA.getComponent();
					oper.setRI(tc.getProperty("bl_rev_object_name"), tc.getProperty("bl_rev_awp0Item_item_id"));
				} else if (childA.equals("Мерительный инструмент")) {
					TCComponent tc = (TCComponent) tcA.getComponent();
					oper.setMI(tc.getProperty("bl_rev_object_name"), tc.getProperty("bl_rev_awp0Item_item_id"));
				} else if (childA.equals("Нормативные документы")) {
					String typeO = ((TCComponent) tcA.getComponent()).getProperty("fnd0bl_line_object_type");
					if (typeO.equals("K7_iDocument Revision")) {
						String IOT = ((TCComponent) tcA.getComponent()).getProperty("bl_rev_object_name");
						if (!IOT.isEmpty() && IOT.length() > 13)
							oper.setIOT(IOT.substring(0, 13), "0");
					}
				} else if (childA.equals("Оборудование")) {
					Map<String, String> mp = ((TCComponentBOMLine) tcA.getComponent()).getItemRevision().getClassificationAttributes();
					String model = "", numId = "", NameEq = "";
					if (mp.containsKey("Модель")) {
						model = mp.get("Модель");
					}
					if (mp.containsKey("Инвентарный номер")) {
						numId = mp.get("Инвентарный номер");
					}
					NameEq = ((TCComponent) tcA.getComponent()).getProperty("bl_rev_object_name");
					if (!model.isEmpty()) {
						int iModel = NameEq.indexOf(model);
						if (iModel > 0) {
							NameEq = NameEq.substring(0, iModel - 1);
						}
					}
					Transition allProp = new Transition();
					allProp.setName(NameEq);
					allProp.setNumber(numId);
					allProp.setMode(model);
					oper.setEquip(allProp);
				} else if (childA.equals("Исполнитель")) {
					Map<String, String> mp = ((TCComponentBOMLine) tcA.getComponent()).getItemRevision().getClassificationAttributes();
					oper.setKodProf(mp.get("Код профессии"));
					oper.setRaz(mp.get("Разряд"));
				} else if (childA.equals("Заготовка")) {
					dateT[10] = ((TCComponent) tcA.getComponent()).getProperty("K7_WPMassa");
					System.out.print(dateT[10]);
				}

			}
			listOper.add(oper);
			xI.addAndGet(1);
			System.out.println(Arrays.toString(activityChildLines));
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (progressBarDialog.getStatusStop()) {
						new MessageDialog("Message", "Процесс прерван", 250, 85, true);
					}
					progressBarDialog.setLabel("" + xI + "/" + nTar);
					progressBarDialog.setValue(xI.get());
				}
			});
		}
		if (listOper.size() > 0 && !progressBarDialog.getStatusStop()) {
			createDocument(listOper);
		} else if (!progressBarDialog.getStatusStop()) {
			progressBarDialog.setLabel("" + nTar + "/" + nTar);
			progressBarDialog.setValue(nTar);
			new MessageDialog("Message", "В выбранном объекте отсутстуют операции", 380, 85, true);
		}

		progressBarDialog.disposeDialog();
		return listOper;
	}

    //рекурсивынй поиск объекта Технологического объекта (необходим если выбран не он сам а родительский или дочерний элемент)
	public void rekPoiskAllOperation(TCComponent tcc0, ListOperation masOper) throws TCException {
		AIFComponentContext[] childrenDo = tcc0.getChildren();
		boolean rP = false, rO = true;
		for (int i = 0; i < childrenDo.length; i++) {
			AIFComponentContext childDo = childrenDo[i];
			TCComponent target = (TCComponent) childDo.getComponent();
			if (target.getProperty("bl_item_object_type").equals("Операция")) {
				rO = false;
				masOper.addOper(target);
			}
			if (target.getProperty("bl_item_object_type").equals("Технологический процесс")) {
				rP = true;
				break;
			}
		}

		if (rO && rP) {
			int l = childrenDo.length;
			for (int i = 0; i < l; i++) {
				AIFComponentContext childDo = childrenDo[i];
				TCComponent target = (TCComponent) childDo.getComponent();
				if (target.getChildren().length > 0) {
					rekPoiskAllOperation(target, masOper);
				}
			}
		}
	}

    //рекурсивный поиск Технологического маршрута 
	public void rekPoiskTarget(TCComponent tcc0, InformTarget inf) throws TCException {
		if (tcc0.getProperty("bl_item_object_type").equals("Технологический маршрут")) {
			AIFComponentContext[] childrenDo = tcc0.getChildren();
			for (int i = 0; i < childrenDo.length; i++) {
				AIFComponentContext childDo = childrenDo[i];
				TCComponent target = (TCComponent) childDo.getComponent();
				if (target.getProperty("bl_occ_type").equals("Цель") && !inf.getR()) {
					inf.setR(true);
					inf.setTm(tcc0);
					break;
				}
			}

			if (!inf.getR()) {
				for (int i = 0; i < childrenDo.length; i++) {
					AIFComponentContext childDo = childrenDo[i];
					TCComponent target = (TCComponent) childDo.getComponent();
					rekPoiskTarget(target, inf);
				}
			}
		}
	}

    //Класс для хранения данных одного объекта тех процесса
	public class InformTarget {
		private boolean r;
		TCComponent tm;

		public void setR(boolean r0) {
			r = r0;
		}

		public boolean getR() {
			return r;
		}

		public void setTm(TCComponent tm0) {
			if (tm == null) {
				tm = tm0;
			}
		}

		public TCComponent getTm() {
			return tm;
		}
	}

    //класс для хранения списка TCComponent(необходим для прохождения в рекурсивной функции)
	public class ListOperation {
		private List<TCComponent> listOper = new ArrayList<>();

		public void addOper(TCComponent oper) {
			listOper.add(oper);
		}

		public List<TCComponent> getListOper() {
			return listOper;
		}
	}

    //Класс для объединения массивов TCComponent
	public AIFComponentContext[] MasPlus(AIFComponentContext[] mas, AIFComponentContext[] masPlus) {
		AIFComponentContext[] mas_masPlus;
		if (masPlus != null && masPlus.length > 0) {
			mas_masPlus = new AIFComponentContext[mas.length + masPlus.length];
			for (int i = 0; i < mas.length; i++) {
				mas_masPlus[i] = mas[i];
			}
			for (int i = 0; i < masPlus.length; i++) {
				mas_masPlus[i + mas.length] = masPlus[i];
			}
			return mas_masPlus;
		} else {
			return mas;
		}
	}

     // класс для хранения данных одной операции, также конструктор геттеры и сеттеры
	class Operation {
		String number, name, kodProf, raz, intensity, mZ;
		Transition equipment;
		List<Revision> sIZ, vI, rI, mI, iOT, KU;
		List<Transition> listT;

		public void setNumber(String number0) {
			number = number0;
			sIZ = new ArrayList<>();
			vI = new ArrayList<>();
			rI = new ArrayList<>();
			mI = new ArrayList<>();
			iOT = new ArrayList<>();
			listT = new ArrayList<>();
		}

		public String getNumber() {
			return number;
		}

		public void setKodProf(String kodProf0) {
			kodProf = kodProf0;
		}

		public String getKodProf() {
			return kodProf;
		}

		public void setIntensity(String intensity0) {
			intensity = intensity0;
		}

		public String getIntensity() {
			return intensity;
		}

		public void setRaz(String raz0) {
			raz = raz0;
		}

		public String getRaz() {
			return raz;
		}

		public void setName(String name0) {
			name = name0;
		}

		public String getName() {
			return name;
		}

		public void setMz(String mZ0) {
			mZ = mZ0;
		}

		public String getMz() {
			return mZ;
		}

		public void setKU(List<Revision> ku0) {
			KU = ku0;
		}

		public List<Revision> getKU() {
			return KU;
		}

		public void setEquip(Transition equip0) {
			equipment = equip0;
		}

		public Transition getEquip() {
			return equipment;
		}

		public void setIOT(String iot, String identiF) {
			Revision r = new Revision(iot, identiF);
			iOT.add(r);
		}

		public List<Revision> getIOT() {
			return iOT;
		}

		public void setSIZ(String siz, String identiF) {
			Revision r = new Revision(siz, identiF);
			sIZ.add(r);
		}

		public List<Revision> getSIZ() {
			return sIZ;
		}

		public void setVI(String vi, String identiF) {
			Revision r = new Revision(vi, identiF);
			vI.add(r);
		}

		public List<Revision> getVI() {
			return vI;
		}

		public void setRI(String ri, String identiF) {
			Revision r = new Revision(ri, identiF);
			rI.add(r);
		}

		public List<Revision> getRI() {
			return rI;
		}

		public void setMI(String mi, String identiF) {
			Revision r = new Revision(mi, identiF);
			mI.add(r);
		}

		public List<Revision> getMI() {
			return mI;
		}

		public void setTran(Transition tr) {
			listT.add(tr);
		}

		public List<Transition> getTran() {
			return listT;
		}

		public List<Transition> getTranSort() {
			return SortTran(listT);
		}
	}

    // класс для хранения всех данных подного перехода, также конструктор геттеры и сеттеры
	class Transition {
		String number, name, mode, equipment, ku;

		public void setNumber(String number0) {
			number = number0;
		}

		public String getNumber() {
			return number;
		}

		public void setName(String name0) {
			name = name0;
		}

		public String getName() {
			return name;
		}

		public void setMode(String mode0) {
			mode = mode0;
		}

		public String getMode() {
			return mode;
		}

		public void setEqup(String equipment0) {
			equipment = equipment0;
		}

		public String getEqup() {
			return equipment;
		}

		public void setKU(String ku0) {
			ku = ku0;
		}

		public String getKU() {
			return ku;
		}
	}

    //класс для хранения всех данных одной ревизии, также конструктор геттеры и сеттеры
	class Revision {
		String name, identiF;

		public Revision(String name0, String identiF0) {
			name = name0;
			identiF = identiF0;
		}

		public String getName() {
			return name;
		}

		public String getIden() {
			return identiF;
		}
	}

    // главный метод для формирования документа PDF (состоит из под методов)
	public void createDocument(List<Operation> listOp) throws DocumentException, TCException, IOException {
		// Создание документа
		Document document = new Document();
		Rectangle art = new Rectangle(PageSize.A4.rotate());
		document.setPageSize(PageSize.A4.rotate());// A4
		document.setMargins(0f, 0f, 18f, 10f);
		art = new Rectangle(PageSize.A4.rotate());

		// Получение данных шапки

		String filename = "МК_Маршрутная_карта_для_АКП2_" + nameDocument;

		String folderPath = System.getProperty("user.dir") + "\\temp\\";
		Util.createDirectory(folderPath);
		String filePath = folderPath + filename + ".pdf";

		// String RESULT = "D:\\MyFiles\\" + filename + ".pdf";
		boolean rWrite = false, reName = false;
		int v = 0;
		while (!rWrite) {
			try {
				PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
				writer.setBoxSize("art", art);
				rWrite = true;
			} catch (Exception e) {
				v++;
				filePath = folderPath + filename + " (" + v + ")" + ".pdf";
				reName = true;
			}
		}
		// writer.setPageEvent(new HeaderFooter(typeDOC));

		PdfPTable tableVKY = new PdfPTable(1);
		tableVKY.setTotalWidth(new float[] { 804 });
		tableVKY.setLockedWidth(true);

		document.open();
		if (tableVKY.size() != 0) {
			document.add(tableVKY); // добавление таблицы в документ
			document.newPage();
		} else {
			createHeader(tableVKY);

			List<PdfPTable> docMas = new ArrayList<>();
			createDateToTable(tableVKY, listOp, docMas);
			dateT[29] = "" + docMas.size();

			boolean r = true;
			for (PdfPTable p : docMas) {
				if (r) {
					createFooter(p, 1);
					r = false;
				}
				document.add(p);
				document.newPage();
			}
			System.out.print("Заполнил");
			// document.add(tableVKY);
			document.newPage();
		}
		MessageBox messageBox;
		document.close();
		TCComponentItemRevision itemRevision = Util.getItemRevision((TCComponentBOMLine) tCComponent2);
		if (Util.checkAccessorsPrivilege(itemRevision, TCAccessControlService.WRITE)) {
			Util.createDataset(session, itemRevision, folderPath, filename);
			String mesage = "Отчет сформирован";
			if (reName) {
				mesage += " и был переименован на \"" + filename + " (" + v + ")" + "\", поскольку отчёт с таким же именем был открыт в другой программе.";
			}
			messageBox = new MessageBox(mtdDialog, mesage, "Информация", MessageBox.INFORMATION);
		} else {
			messageBox = new MessageBox(mtdDialog, "Нет прав на запись в псевдопапку \"Технологическая документация\", отчет расположен в \"" + folderPath + "\"", "Информация", MessageBox.INFORMATION);
		}

		messageBox.setVisible(true);
	}

    //метод для формирования в документе главной шапки (Header) (первая страница отличается от всех остальных)
	public void createHeader(PdfPTable tableVKY) throws DocumentException, IOException {
		// Первая строка
		PdfPTable dataTable = new PdfPTable(1);
		dataTable.setTotalWidth(new float[] { 804f }); // 354f,450f
		// PdfPCell cell = createTextCell2("", 12f, 0, 0, 0, 0, 0, 0, 0);
		PdfPCell cell2 = createTextCell2("Ф-07 СТО КАМАЗ 02.01", 12f, 2, 1, 0, 6, 0, -7, -2);
		cell2.setPaddingRight(1f);
		// dataTable.addCell(cell);
		dataTable.addCell(cell2);
		PdfPCell cellList = new PdfPCell(dataTable);
		// Вторая строка
		PdfPTable dataTable2 = new PdfPTable(3);
		dataTable2.setTotalWidth(new float[] { 61f, 254.5f, 499.7f });
		PdfPCell cell3 = createTextCell2("АКП2", 53f, 1, 1, 0, 12, 1, 0, -7);
		PdfPCell cell4 = createTextCell2("Маршрутная карта", 53f, 1, 1, 0, 12, 1, 0, -7);
		PdfPCell cell5 = createTextCell2("", 53f, 2, 1, 0, 6, 0, 0, 0);
		dataTable2.addCell(cell3);
		dataTable2.addCell(cell4);
		// Вторая внутренняя ячейка 1
		PdfPTable dataTable3 = new PdfPTable(2);
		dataTable3.setTotalWidth(new float[] { 244f, 244.5f });
		PdfPCell cell6 = createTextCell2(dateT[1], 19f, 1, 1, 0, 8, 1, 0, -4);
		PdfPCell cell7 = createTextCell2(dateT[32], 19f, 1, 1, 0, 8, 1, 0, -4);
		dataTable3.addCell(cell6);
		dataTable3.addCell(cell7);
		PdfPCell cellList3 = new PdfPCell(dataTable3);
		// Вторая внутренняя ячейка 2
		PdfPTable dataTable4 = new PdfPTable(3);
		dataTable4.setTotalWidth(new float[] { 244f, 47f, 197.5f });
		PdfPCell cell8 = createTextCell2(dateT[2], 34f, 1, 1, 0, 10, 1, 0, -6);
		PdfPCell cell9 = createTextCell2("", 34f, 1, 1, 0, 12, 1, 0, 0);
		PdfPCell cell10 = createTextCell2(dateT[3], 34f, 1, 1, 0, 10, 1, 0, -6);
		dataTable4.addCell(cell8);
		dataTable4.addCell(cell9);
		dataTable4.addCell(cell10);
		PdfPCell cellList4 = new PdfPCell(dataTable4);
		PdfPTable dataTable5 = new PdfPTable(1);
		dataTable5.addCell(cellList3);
		dataTable5.addCell(cellList4);
		cell5 = new PdfPCell(dataTable5);
		dataTable2.addCell(cell5); // завершение второй строки
		PdfPCell cellList2 = new PdfPCell(dataTable2);

		// Третья строка
		PdfPTable dataTable6 = new PdfPTable(7);
		dataTable6.setTotalWidth(new float[] { 237.7f, 77.6f, 49.7f, 327.9f, 44.7f, 45.5f, 32.1f });
		PdfPCell cell11;
		{ // Внутренняя часть cell11
			PdfPTable dataTable6_0 = new PdfPTable(1);
			dataTable6_0.setTotalWidth(new float[] { 237.7f });
			PdfPCell cell11_1 = createTextCell2("МАТЕРИАЛ", 11f, 1, 1, 0, 6, 0, 0, -3);
			PdfPTable dataTable6_1 = new PdfPTable(2);
			dataTable6_1.setTotalWidth(new float[] { 179.7f, 58f });
			PdfPCell cell11_2 = createTextCell2("Наименование, марка", 29f, 1, 1, 0, 8, 0, 0, -5);
			PdfPCell cell11_3 = createTextCell2("Код", 29f, 1, 1, 0, 8, 0, 0, -5);
			dataTable6_1.addCell(cell11_2);
			dataTable6_1.addCell(cell11_3);
			PdfPCell cell11_23 = new PdfPCell(dataTable6_1);
			dataTable6_0.addCell(cell11_1);
			dataTable6_0.addCell(cell11_23);
			cell11 = new PdfPCell(dataTable6_0);
		}

		PdfPCell cell12 = createTextCell2("Код единицы величины", 40f, 1, 1, 0, 8, 0, 0, -5);
		PdfPCell cell13 = createTextCell2("Масса детали", 40f, 1, 1, 0, 8, 0, 0, -5);
		PdfPCell cell14;
		{ // Внутренняя часть cell41
			PdfPTable dataTable6_2 = new PdfPTable(1);
			dataTable6_2.setTotalWidth(new float[] { 327.9f });
			PdfPCell cell14_1 = createTextCell2("ЗАГОТОВКА", 11f, 1, 1, 0, 6, 0, 0, -3);
			PdfPTable dataTable6_3 = new PdfPTable(4);
			dataTable6_3.setTotalWidth(new float[] { 109.7f, 85.3f, 56.6f, 76.3f });
			PdfPCell cell14_2 = createTextCell2("Код и вид", 29f, 1, 1, 0, 8, 0, 0, -5);
			PdfPCell cell14_3 = createTextCell2("Профиль и размеры", 29f, 1, 1, 0, 8, 0, 0, -5);
			PdfPCell cell14_4 = createTextCell2("Количество заготовок", 29f, 1, 1, 0, 8, 0, 0, -5);
			PdfPCell cell14_5 = createTextCell2("Масса", 29f, 1, 1, 0, 8, 0, 0, -5);
			dataTable6_3.addCell(cell14_2);
			dataTable6_3.addCell(cell14_3);
			dataTable6_3.addCell(cell14_4);
			dataTable6_3.addCell(cell14_5);
			PdfPCell cell14_23 = new PdfPCell(dataTable6_3);
			dataTable6_2.addCell(cell14_1);
			dataTable6_2.addCell(cell14_23);
			cell14 = new PdfPCell(dataTable6_2);
		}
		PdfPCell cell15 = createTextCell2("Еденицы номиро- вания", 40f, 1, 1, 1, 8, 0, -4, 0);
		PdfPCell cell16 = createTextCell2("Норма расхода", 40f, 1, 1, 1, 8, 0, -4, 0);
		PdfPCell cell17 = createTextCell2("Кол-во деталей", 40f, 1, 1, 1, 8, 0, -4, 0);
		dataTable6.addCell(cell11);
		dataTable6.addCell(cell12);
		dataTable6.addCell(cell13);
		dataTable6.addCell(cell14);
		dataTable6.addCell(cell15);
		dataTable6.addCell(cell16);
		dataTable6.addCell(cell17);
		PdfPCell cellList6 = new PdfPCell(dataTable6);

		// Четвертая строка
		PdfPTable dataTable7 = new PdfPTable(11);
		dataTable7.setTotalWidth(new float[] { 179.7f, 58f, 77.6f, 49.7f, 109.7f, 85.3f, 56.6f, 76.3f, 44.7f, 45.5f, 32.1f });
		PdfPCell cell18 = createTextCell2(dateT[4], 12f, 1, 1, 0, 9, 0, 0, -6);
		PdfPCell cell19 = createTextCell2("", 12f, 1, 1, 0, 9, 0, 0, 0);
		PdfPCell cell20 = createTextCell2(dateT[5], 12f, 1, 1, 0, 9, 0, 0, -6);
		PdfPCell cell21 = createTextCell2(dateT[6], 12f, 1, 1, 0, 9, 0, 0, -6);
		PdfPCell cell22 = createTextCell2(dateT[7], 12f, 1, 1, 0, 9, 0, 0, -6);
		PdfPCell cell23 = createTextCell2(dateT[8], 12f, 1, 1, 0, 9, 0, 0, -6);
		PdfPCell cell24 = createTextCell2(dateT[9], 12f, 1, 1, 0, 9, 0, 0, -6);
		PdfPCell cell25 = createTextCell2(dateT[10], 12f, 1, 1, 0, 9, 0, 0, -6);
		PdfPCell cell26 = createTextCell2("", 12f, 1, 1, 0, 9, 0, 0, 0);
		PdfPCell cell27 = createTextCell2("", 12f, 1, 1, 0, 9, 0, 0, 0);
		PdfPCell cell28 = createTextCell2(dateT[11], 12f, 1, 1, 0, 9, 0, 0, -6);
		dataTable7.addCell(cell18);
		dataTable7.addCell(cell19);
		dataTable7.addCell(cell20);
		dataTable7.addCell(cell21);
		dataTable7.addCell(cell22);
		dataTable7.addCell(cell23);
		dataTable7.addCell(cell24);
		dataTable7.addCell(cell25);
		dataTable7.addCell(cell26);
		dataTable7.addCell(cell27);
		dataTable7.addCell(cell28);
		PdfPCell cellList7 = new PdfPCell(dataTable7);

		tableVKY.addCell(cellList);
		tableVKY.addCell(cellList2);
		tableVKY.addCell(cellList6);
		tableVKY.addCell(cellList7);
		createHeaderOne(tableVKY, 1);
	}

    //метод для формирования в документе шапки на последующих страницах (Header) 
	public void createHeaderOne(PdfPTable tableVKY, int list) throws DocumentException, IOException {
		// Шапка на каждую страницу
		if (list >= 2) {
			PdfPTable dataTable8_0 = new PdfPTable(2);
			dataTable8_0.setTotalWidth(new float[] { 600f, 204.4f });
			PdfPCell cell = createTextCell2("" + dateT[1] + " " + dateT[2] + " " + dateT[3], 12f, 1, 1, 0, 6, 0, -7, -2);
			PdfPCell cell2 = createTextCell2("Ф-07 СТО КАМАЗ 02.01", 12f, 2, 1, 0, 6, 0, -7, -2);
			dataTable8_0.addCell(cell);
			dataTable8_0.addCell(cell2);
			PdfPCell cellList8_0 = new PdfPCell(dataTable8_0);
			tableVKY.addCell(cellList8_0);
		}
		PdfPTable dataTable8 = new PdfPTable(9);
		dataTable8.setTotalWidth(new float[] { 46f, 319.4f, 109.7f, 85.3f, 32.8f, 23.8f, 76.3f, 90.2f, 32.1f });
		PdfPCell cell29;
		{ // Внутренняя часть cell29
			PdfPTable dataTable8_0 = new PdfPTable(1);
			dataTable8_0.setTotalWidth(new float[] { 46f });
			PdfPCell cell29_1 = createTextCell2("Номер", 13f, 1, 0, 0, 8, 0, 0, -2);
			PdfPTable dataTable8_1 = new PdfPTable(3);
			dataTable8_1.setTotalWidth(new float[] { 12.6f, 12.6f, 21f });
			PdfPCell cell29_2 = createTextCell2("Цеха", 66.8f, 1, 1, 1, 8, 0, -5, 0);
			PdfPCell cell29_3 = createTextCell2("Участка", 66.8f, 1, 1, 1, 8, 0, -5, 0);
			PdfPCell cell29_4 = createTextCell2("Операций", 66.8f, 1, 1, 1, 8, 0, -5, 0);
			dataTable8_1.addCell(cell29_2);
			dataTable8_1.addCell(cell29_3);
			dataTable8_1.addCell(cell29_4);
			PdfPCell cell29_23 = new PdfPCell(dataTable8_1);
			dataTable8_0.addCell(cell29_1);
			dataTable8_0.addCell(cell29_23);
			cell29 = new PdfPCell(dataTable8_0);
		}
		PdfPCell cell30 = createTextCell2("Наименование и содержание операции", 79.8f, 1, 1, 0, 8, 0, 0, 0);
		PdfPCell cell31 = createTextCell2("Обозначение документа", 79.8f, 1, 1, 0, 8, 0, 0, 0);
		PdfPCell cell32 = createTextCell2("Оборудование", 79.8f, 1, 1, 0, 8, 0, 0, 0);
		PdfPCell cell33;
		{ // Внутренняя часть cell33
			PdfPTable dataTable8_2 = new PdfPTable(1);
			dataTable8_2.setTotalWidth(new float[] { 32.8f });
			PdfPCell cell33_1 = createTextCell2("Коэфф. шт. пр", 36.8f, 1, 1, 1, 8, 0, -3, 0);
			PdfPCell cell33_2 = createTextCell2("Код профессии", 43f, 1, 1, 1, 8, 0, -3, 0);
			dataTable8_2.addCell(cell33_1);
			dataTable8_2.addCell(cell33_2);
			cell33 = new PdfPCell(dataTable8_2);
		}
		PdfPCell cell34;
		{ // Внутренняя часть cell34
			PdfPTable dataTable8_3 = new PdfPTable(1);
			dataTable8_3.setTotalWidth(new float[] { 23.8f });
			PdfPCell cell34_1 = createTextCell2("Кол. раб.", 36.8f, 1, 1, 1, 8, 0, -3, 0);
			PdfPCell cell34_2 = createTextCell2("Разр. раб.", 43f, 1, 1, 1, 8, 0, -3, 0);
			dataTable8_3.addCell(cell34_1);
			dataTable8_3.addCell(cell34_2);
			cell34 = new PdfPCell(dataTable8_3);
		}
		PdfPCell cell35 = createTextCell2("Ф.И.О. специалиста", 79.8f, 1, 1, 0, 8, 0, 0, 0);
		PdfPCell cell36 = createTextCell2("Дата приема, подпись, штамп ОТК", 79.8f, 1, 1, 0, 8, 0, 0, 0);
		PdfPCell cell37;
		{ // Внутренняя часть cell37
			PdfPTable dataTable8_4 = new PdfPTable(1);
			dataTable8_4.setTotalWidth(new float[] { 32.1f });
			PdfPCell cell37_1 = createTextCell2("Т п.т.", 36.8f, 1, 1, 0, 8, 0, 0, -4);
			PdfPCell cell37_2 = createTextCell2("Т ш.т.", 43f, 1, 1, 0, 8, 0, 0, -4);
			dataTable8_4.addCell(cell37_1);
			dataTable8_4.addCell(cell37_2);
			cell37 = new PdfPCell(dataTable8_4);
		}
		dataTable8.addCell(cell29);
		dataTable8.addCell(cell30);
		dataTable8.addCell(cell31);
		dataTable8.addCell(cell32);
		dataTable8.addCell(cell33);
		dataTable8.addCell(cell34);
		dataTable8.addCell(cell35);
		dataTable8.addCell(cell36);
		dataTable8.addCell(cell37);
		PdfPCell cellList8 = new PdfPCell(dataTable8);

		tableVKY.addCell(cellList8);

	}

    ////метод для формирования в документе завершающего контекста страницы (Footer) 
	public void createFooter(PdfPTable tableVKY, int list) throws DocumentException, IOException {
		PdfPTable footerAll = new PdfPTable(2);
		footerAll.setTotalWidth(new float[] { 26f, 1177f });

		PdfPTable footerleft = new PdfPTable(1);
		PdfPCell cellLeft = new PdfPCell(footerleft);
		footerAll.addCell(cellLeft);

		PdfPTable footerRight = new PdfPTable(1);

		PdfPTable dataTable9_1 = new PdfPTable(15);
		dataTable9_1.setTotalWidth(new float[] { 34f, 43f, 77f, 115f, 60f, 46f, 71f, 62f, 58f, 105f, 81f, 151f, 157f, 78f, 37f });
		String str = "";
		for (int i = 0; i < 15; i++) {
			str = "";
			if (i == 10) {
				str = "Разработал";
			} else if (i == 11) {
				str = dateT[26];
			} else if (i == 14) {
				str = "Лист";
			}
			PdfPCell cell = createTextCell2(str, 14.4f, 1, 0, 0, 8, 0, 2, -1);
			dataTable9_1.addCell(cell);
		}
		PdfPCell cellList9_1 = new PdfPCell(dataTable9_1);
		footerRight.addCell(cellList9_1);

		PdfPTable dataTable9_2 = new PdfPTable(15);
		dataTable9_2.setTotalWidth(new float[] { 34f, 43f, 77f, 115f, 60f, 46f, 71f, 62f, 58f, 105f, 81f, 151f, 157f, 78f, 37f });
		if (list == 1) {
			for (int i = 0; i < 15; i++) {
				str = "";
				if (i == 14) {
					str = "" + list;
				}
				PdfPCell cell = createTextCell2(str, 14.4f, 1, 0, 0, 8, 0, 2, -1);
				dataTable9_2.addCell(cell);
			}
		}
		PdfPCell cellList9_2 = new PdfPCell(dataTable9_2);
		footerRight.addCell(cellList9_2);

		PdfPTable dataTable9_3 = new PdfPTable(15);
		dataTable9_3.setTotalWidth(new float[] { 34f, 43f, 77f, 115f, 60f, 46f, 71f, 62f, 58f, 105f, 81f, 151f, 157f, 78f, 37f });
		for (int i = 0; i < 15; i++) {
			str = "";
			if (i == 10) {
				str = "Проверил";
			} else if (i == 11) {
				str = dateT[27];
			} else if (i == 14 && list > 1) {
				str = "" + list;
			}
			PdfPCell cell = createTextCell2(str, 14.4f, 1, 0, 0, 8, 0, 2, -1);
			dataTable9_3.addCell(cell);
		}
		PdfPCell cellList9_3 = new PdfPCell(dataTable9_3);
		footerRight.addCell(cellList9_3);

		PdfPTable dataTable9_4 = new PdfPTable(15);
		dataTable9_4.setTotalWidth(new float[] { 34f, 43f, 77f, 115f, 60f, 46f, 71f, 62f, 58f, 105f, 81f, 151f, 157f, 78f, 37f });
		if (list == 1) {
			for (int i = 0; i < 15; i++) {
				str = "";
				if (i == 14) {
					str = "Л-ов";
				}
				PdfPCell cell = createTextCell2(str, 14.4f, 1, 0, 0, 8, 0, 2, -1);
				dataTable9_4.addCell(cell);
			}
		}
		PdfPCell cellList9_4 = new PdfPCell(dataTable9_4);
		footerRight.addCell(cellList9_4);

		PdfPTable dataTable9_5 = new PdfPTable(15);
		dataTable9_5.setTotalWidth(new float[] { 34f, 43f, 77f, 115f, 60f, 46f, 71f, 62f, 58f, 105f, 81f, 151f, 157f, 78f, 37f });
		for (int i = 0; i < 15; i++) {
			str = "";
			if (i == 0 || i == 5) {
				str = "Изм";
			} else if (i == 1 || i == 6) {
				str = "Лист";
			} else if (i == 2 || i == 7) {
				str = "№ докум.";
			} else if (i == 3 || i == 8) {
				str = "Подпись";
			} else if (i == 4 || i == 9) {
				str = "Дата";
			} else if (i == 10) {
				str = "Н. контр";
			} else if (i == 14) {
				str = dateT[29];
			}
			PdfPCell cell = createTextCell2(str, 14.4f, 1, 0, 0, 8, 0, -1, -1);
			dataTable9_5.addCell(cell);
		}
		PdfPCell cellList9_5 = new PdfPCell(dataTable9_5);
		footerRight.addCell(cellList9_5);
		PdfPCell cellRight = new PdfPCell(footerRight);
		footerAll.addCell(cellRight);
		PdfPCell cellAll = new PdfPCell(footerAll);
		tableVKY.addCell(cellAll);
	}

    //метод для выгрузки данных из выгруженного списка объектов (Операции, переходы, действия и т д всё что требовалось в ТЗ)
	public void createDateToTable(PdfPTable tableVKY, List<Operation> list, List<PdfPTable> doc) throws DocumentException, IOException {

		System.out.print("qwer_______" + list.size());
		// Заполнение данных
		PdfPTable dataTableDate0 = new PdfPTable(11);
		dataTableDate0.setTotalWidth(new float[] { 12.6f, 12.6f, 21f, 319.4f, 109.7f, 85.3f, 32.8f, 23.8f, 76.3f, 90.2f, 32.1f });
		for (int i = 0; i < 11; i++) {
			PdfPCell cellNull = createTextCell2("", 14.4f, 0, 0, 0, 0, 0, 0, 0);
			dataTableDate0.addCell(cellNull);
		}
		PdfPCell cellListDate = new PdfPCell(dataTableDate0);
		tableVKY.addCell(cellListDate);
		int nStr = 0, nStrOne = 0, nStrList = 0, nOp = 0;
		for (Operation op : list) {
			List<String> list3 = new ArrayList<>(), list4 = new ArrayList<>(), list5 = new ArrayList<>();
			// op.generateKompanovka(57, 17);
			System.out.print(op.getTran().size());
			if (op.getKU() != null && op.getKU().size() > 0) {
				list3.addAll(kompanovkaKu(op.getKU()));
			}
			for (Transition tr : op.getTranSort()) {
				String nameNum = "";
				if (tr.getNumber().equals("Y")) {
					nameNum = tr.getName();
				} else {
					nameNum = tr.getNumber() + ". " + tr.getName();
				}
				list3.addAll(kompanovkaSTR(nameNum, 4340, 1));
				if (tr.getKU() != null) {
					/*
					 * int lKu = tr.getName().length(); if ((lKu>0)&&tr.getName().charAt(lKu-1)!='
					 * ') {nameNum+=" ";} nameNum+=tr.getKU();
					 */
					list3.addAll(kompanovkaSTR("КУ: " + tr.getKU(), 4160, 1));
				}

				// if (tr.getMode() != null && tr.getMode().length() > 0) { Условие для запрета
				// вывода Инструментов если нет Режима резания
				list3.addAll(kompanovkaSTR(tr.getMode(), 4340, 1));
				if (op.getVI() != null) {
					list3.addAll(kompanovkaList(sborkaInstr(tr.getEqup(), op.getVI()), 4160, "ВИ: "));
				}
				if (op.getRI() != null) {
					list3.addAll(kompanovkaList(sborkaInstr(tr.getEqup(), op.getRI()), 4160, "РИ: "));
				}
				if (op.getMI() != null) {
					list3.addAll(kompanovkaList(sborkaInstr(tr.getEqup(), op.getMI()), 4160, "СИ: "));
				}
				// }
			}
			if (op.getSIZ() != null) {
				list3.addAll(kompanovkaList(sborkaInstr("0", op.getSIZ()), 4100, "СИЗ: "));
			}
			if (op.getIOT() != null) {
				list4.addAll(kompanovkaList(sborkaInstr("0", op.getIOT()), 1500, ""));
			}
			list4.addAll(kompanovkaSTR(dateT[33], MySizeString(dateT[33]), 0));
			if (op.getEquip() != null) {
				if (op.getEquip().getName() != null && op.getEquip().getName().length() > 0) {
					list5.addAll(kompanovkaSTR(op.getEquip().getName(), MySizeString(op.getEquip().getName()), 0));
				}
				if (op.getEquip().getNumber() != null && op.getEquip().getNumber().length() > 0) {
					list5.addAll(kompanovkaSTR(op.getEquip().getNumber(), MySizeString(op.getEquip().getNumber()), 0));
				}
				if (op.getEquip().getMode() != null && op.getEquip().getMode().length() > 0) {
					list5.addAll(kompanovkaSTR("(" + op.getEquip().getMode() + ")", MySizeString("(" + op.getEquip().getMode() + ")"), 0));
				}
			}
			int k = list3.size() + 1;
			if (list4.size() > k) {
				k = list4.size();
			}
			;
			if (list5.size() > k) {
				k = list5.size();
			}

			for (int i = 0; i < k; i++) {
				PdfPTable dataTableDate = new PdfPTable(11);
				dataTableDate.setTotalWidth(new float[] { 12.6f, 12.6f, 21f, 319.4f, 109.7f, 85.3f, 32.8f, 23.8f, 76.3f, 90.2f, 32.1f });
				PdfPCell cellDate = createTextCell2("", 14.4f, 0, 0, 0, 8, 0, 0, -4);
				dataTableDate.addCell(cellDate);
				cellDate = createTextCell2("", 14.4f, 0, 0, 0, 8, 0, 0, -4);
				dataTableDate.addCell(cellDate);
				String num = "";
				if (i == 0 && op.getNumber() != null) {
					num = op.getNumber();
				}
				cellDate = createTextCell2(num, 14.4f, 1, 0, 0, 8, 0, 0, -1);
				dataTableDate.addCell(cellDate);
				String name = "";
				int style = 0;
				if (i == 0) {
					if (op.getName() != null) {
						name = op.getName();
						style = 1;
					}
				} else {
					if (list3.size() >= i) {
						name = "" + list3.get(i - 1);
						if (list3.get(i - 1).indexOf("Установ ") > 0) {
							style = 1;
						}
					}
				}
				/*if (op.getName().toLowerCase().contains("контроль")&&i>0) {
					cellDate = createTextCell3(name, 14.4f, 0, 0, 0, 8, style, 3, 0);
				}
				else {
					cellDate = createTextCell2(name, 14.4f, 0, 0, 0, 8, style, 3, 0);
				}*/
				cellDate = createTextCell3(name, 14.4f, 0, 0, 0, 8, style, 3, 0); /// полный вывод по новому формату
				
				dataTableDate.addCell(cellDate);

				String IOT = "";
				if (list4.size() > i) {
					IOT = list4.get(i);
				}
				cellDate = createTextCell2(IOT, 14.4f, 1, 1, 0, 8, 0, 0, -2);
				dataTableDate.addCell(cellDate);

				String EQU = "";
				if (list5.size() > i) {
					EQU = list5.get(i);
				}
				cellDate = createTextCell2(EQU, 14.4f, 1, 1, 0, 8, 0, 0, -2);
				dataTableDate.addCell(cellDate);

				String Kod = "";
				if (i == 0 && op.getKodProf() != null) {
					Kod = op.getKodProf();
				}
				cellDate = createTextCell2(Kod, 14.4f, 1, 1, 0, 8, 0, 0, -2);
				dataTableDate.addCell(cellDate);

				String Raz = "";
				if (i == 0 && op.getRaz() != null) {
					Raz = op.getRaz();
				}
				cellDate = createTextCell2(Raz, 14.4f, 1, 1, 0, 8, 0, 0, -2);
				dataTableDate.addCell(cellDate);

				cellDate = createTextCell2("", 14.4f, 0, 0, 0, 8, 0, 0, -4);
				dataTableDate.addCell(cellDate);

				cellDate = createTextCell2("", 14.4f, 0, 0, 0, 8, 0, 0, -4);
				dataTableDate.addCell(cellDate);

				String Inten = "";
				if (i == 0 && op.getIntensity() != null) {
					Inten = op.getIntensity();
				}
				cellDate = createTextCell2(Inten, 14.4f, 1, 1, 0, 8, 0, 0, -2);
				dataTableDate.addCell(cellDate);
				cellListDate = new PdfPCell(dataTableDate);
				tableVKY.addCell(cellListDate);
				nStr++;
				nStrOne++;
				nStrList++;
				int listN = 0;
				if (nStr <= 19) {
					listN = 1;
				} else if (nStr > 19) {
					listN = 1 + (int) Math.ceil(((double) (nStr - 19)) / 30);
				}
				if ((nStr == 19 || (nStr - 19) % 30 == 0) && (nStrOne != k || nOp + 1 != list.size())) {
					if (nStr != 19) {
						createFooter(tableVKY, listN);
					}
					doc.add(tableVKY);
					nStrList = 0;
					tableVKY = new PdfPTable(1);
					tableVKY.setTotalWidth(new float[] { 804 });
					tableVKY.setLockedWidth(true);
					createHeaderOne(tableVKY, listN + 1);
				} else if (nStrOne == k && nOp + 1 == list.size()) {
					if (listN == 1 && nStrList < 19) {
						int nDop = 19 - nStrList;
						for (int nD = 0; nD < nDop; nD++) {
							PdfPTable dataTableDatePos = new PdfPTable(11);
							dataTableDatePos.setTotalWidth(new float[] { 12.6f, 12.2f, 21f, 319.4f, 109.7f, 85.3f, 32.8f, 23.8f, 76.3f, 90.2f, 32.1f });
							for (int ya = 0; ya < 11; ya++) {
								PdfPCell cellNull = createTextCell2("", 14.4f, 0, 0, 0, 0, 0, 0, 0);
								dataTableDatePos.addCell(cellNull);
							}
							PdfPCell cellListDatePos = new PdfPCell(dataTableDatePos);
							tableVKY.addCell(cellListDatePos);
						}
					} else if (listN > 1 && nStrList < 30) {
						int nDop = 30 - nStrList;
						for (int nD = 0; nD < nDop; nD++) {
							PdfPTable dataTableDatePos = new PdfPTable(11);
							dataTableDatePos.setTotalWidth(new float[] { 12.6f, 12.2f, 21f, 319.4f, 109.7f, 85.3f, 32.8f, 23.8f, 76.3f, 90.2f, 32.1f });
							for (int ya = 0; ya < 11; ya++) {
								PdfPCell cellNull = createTextCell2("", 14.4f, 0, 0, 0, 0, 0, 0, 0);
								dataTableDatePos.addCell(cellNull);
							}
							PdfPCell cellListDatePos = new PdfPCell(dataTableDatePos);
							tableVKY.addCell(cellListDatePos);
						}
					}

					if (listN > 1) {
						createFooter(tableVKY, listN);
					}
					doc.add(tableVKY);
					nStrList = 0;
				}
			}
			nStrOne = 0;
			nOp++;
		}

	}

    //вспомогательный метод для формирования объекта текстовой ячейки (используется в createDocument)
	public PdfPCell createTextCell(String text) throws DocumentException, IOException {
		PdfPCell cell = new PdfPCell();
		Paragraph paragraph = new Paragraph(text, setCyrillicFont(12, 0));
		paragraph.setAlignment(Element.ALIGN_CENTER);
		cell.addElement(paragraph);
		cell.setPadding(0);
		cell.setBorder(Rectangle.NO_BORDER);
		return cell;
	}

    //вспомогательный метод для формирования объекта текстовой ячейки2 (используется в createDocument)
	public PdfPCell createTextCell2(String text, float higer, int Xp, int Yp, int angle, int size, int style, int l, int t) throws DocumentException, IOException {
		PdfPCell cell = new PdfPCell();
		Paragraph paragraph = new Paragraph(text, setCyrillicFont(size, style));
		
		if (Xp == 0) {
			paragraph.setAlignment(Element.ALIGN_LEFT);
		} else if (Xp == 1) {
			paragraph.setAlignment(Element.ALIGN_CENTER);
		} else if (Xp == 2) {
			paragraph.setAlignment(Element.ALIGN_RIGHT);
		}

		cell.addElement(paragraph);

		if (Yp == 0) {
			cell.setVerticalAlignment(Element.ALIGN_TOP);
		} else if (Yp == 1) {
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		} else if (Yp == 2) {
			cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
		}

		cell.setFixedHeight(higer);
		cell.setPadding(0);
		cell.setPaddingLeft(l);
		cell.setPaddingTop(t);
		if (t==-25) {cell.setPaddingBottom(-9);}
		if (t==-24) {cell.setPaddingBottom(-16);}
		if (angle == 1)
			cell.setRotation(90);

		return cell;
	}
	
    //вспомогательный метод для формирования объекта текстовой ячейки3 (используется в createDocument)
	public PdfPCell createTextCell3(String text, float higer, int Xp, int Yp, int angle, int size, int style, int l, int t) throws DocumentException, IOException {
		
		try {
			List<Revision> list = kompanovkaKubik(text);
			boolean r = false; for (Revision rev: list) {if (rev.getIden().equals("d")) {r=true; break;}}
			if (!r) {
				return createTextCell2(text, 14.4f, 0, 0, 0, 8, style, l, 0);
				}
			PdfPTable dataTableDate = new PdfPTable(list.size());
			float[] fS = new float[list.size()];
			List<PdfPCell> cellsList = new ArrayList<PdfPCell>();

			int allSize=0;
			for (int i=0;i<list.size()-1;i++) {
				int cellSize = MySizeString(list.get(i).getName().replace("[", ""));
				fS[i] = (float)(cellSize);
				allSize+=cellSize;
			}
			fS[fS.length-1] = (float)(4348-allSize);
			
			dataTableDate.setTotalWidth(fS);
			
			for (int i=0;i<list.size();i++) {
				if (list.get(i).getIden().equals("t")||list.get(i).getIden().equals("p")) {
					int ls=0; if (i==0) {ls=l;}
					PdfPCell cellDate;
					if (i==list.size()-1&&(i>0)&&(list.get(i).getIden().equals("p"))) {cellDate  = createTextCell2("", higer, 0, 1, 0, 8, style, ls, -2);}
					else if (list.get(i).getName().length()==1) {cellDate  = createTextCell2(list.get(i).getName(), higer, 0, 1, 0, 8, style, ls, -2); cellDate.setPaddingLeft(0.2f);}
					else {cellDate  = createTextCell2(list.get(i).getName(), higer, 0, 1, 0, 8, style, ls, -2);}
					//cellDate  = createTextCell2(list.get(i).getName(), higer, 0, 1, 0, 8, style, ls, -2);
					cellDate.setBorder(Rectangle.NO_BORDER);
					dataTableDate.addCell(cellDate);
					/*fS[i] = cellDate.getWidth();
					cellsList.add(cellDate);*/
				}
				else {
					PdfPCell cellDate = createTextCellDopusk(list.get(i).getName(), higer-3.5f);
					dataTableDate.addCell(cellDate);
					/*fS[i] = cellDate.getWidth();
					cellsList.add(cellDate);*/
				}
			}
			
			for (PdfPCell p: cellsList) {
				dataTableDate.addCell(p);
			}
			PdfPCell cellListDate = new PdfPCell(dataTableDate);
			return cellListDate;
		}catch(Exception e) {return createTextCell2(text, 14.4f, 0, 0, 0, 8, style, l, 0);}

	}
	
    //вспомогательный метод для формирования объекта текстовой ячейки Допуск (спец символы)(по ТЗ) (используется в createDocument)
	public PdfPCell createTextCellDopusk(String dopusk,float higer) throws DocumentException, IOException {
		if (dopusk.length()==0) {return createTextCell2("", higer, 0, 0, 0, 7, 0, 0, -2);}
		List<String> listS= new ArrayList<>();
		int n=0; String str="";
		for (int i=0;i<dopusk.length();i++) {
			if (dopusk.charAt(i)=='[') {
				str="";
			}
			else if (dopusk.charAt(i)==']'){
				listS.add(str);
			}
			else {str+=dopusk.charAt(i);}
		}
		if (listS.size()==0) {return createTextCell2("", higer, 0, 0, 0, 7, 0, 0, -2);}
		PdfPTable dataTableDate = new PdfPTable(listS.size());
		float[] fS = new float[listS.size()];
		for (int i=0;i<listS.size();i++) {
			fS[i] = MySizeString(listS.get(i));
		}
		dataTableDate.setTotalWidth(fS);
		for (int i=0;i<listS.size();i++) {
		PdfPCell cellDate;

		//спец символы
		if (listS.get(i).indexOf(String.valueOf((char) 9711))>=0) {cellDate = createTextCell2(listS.get(i), higer, 1, 2, 0, 14, 0, 0, -16);} //круг+++
		else if (listS.get(i).indexOf(String.valueOf((char) 8212))>=0) {cellDate = createTextCell2(listS.get(i), higer, 1, 1, 0, 8, 0, 0, -8);} //линия горизонтальная+++
		else if (listS.get(i).indexOf(String.valueOf((char) 9649))>=0) {cellDate = createTextCell2(listS.get(i), higer, 1, 1, 0, 22, 0, 0, -25);} //параллелограмм+++
		else if (listS.get(i).indexOf(String.valueOf((char) 9005))>=0) {cellDate = createTextCell2(listS.get(i), higer, 1, 1, 0, 18, 0, 0, -18);} //круг между линиями под наклоном+++
		else if (listS.get(i).indexOf(String.valueOf((char) 61))>=0) {cellDate = createTextCell2(listS.get(i), higer, 1, 1, 0, 12, 0, 0, -8);} //равно+++
		else if (listS.get(i).indexOf(String.valueOf((char) 8725))>=0) {cellDate = createTextCell2(listS.get(i), higer, 1, 1, 0, 11, 0, 0, -9);} //наклонные две линии+++
		else if (listS.get(i).indexOf(String.valueOf((char) 9162))>=0) {cellDate = createTextCell2(""+String.valueOf((char) 8869), higer, 1, 0, 0, 13, 0, 0, -10);} //перпендикулярность+++
		else if (listS.get(i).indexOf(String.valueOf((char) 10655))>=0) {cellDate = createTextCell2(String.valueOf((char) 8736), higer, 1, 1, 0, 16, 0, 0, -25);} //угол+++
		else if (listS.get(i).indexOf(String.valueOf((char) 9022))>=0) {cellDate = createTextCell2(listS.get(i), higer, 1, 1, 0, 16, 0, 0, -16);} //круг в круге+++
		else if (listS.get(i).indexOf(String.valueOf((char) 9007))>=0) {cellDate = createTextCell2(listS.get(i), higer, 1, 1, 0, 14, 0, 0, -14);} //кнопка+++
		else if (listS.get(i).indexOf(String.valueOf((char) 8982))>=0) {cellDate = createTextCell2(listS.get(i), higer, 1, 1, 0, 15, 0, 0, -15);} //прицел+++
		else if (listS.get(i).indexOf(String.valueOf((char) 10005))>=0) {cellDate = createTextCell2(listS.get(i), higer, 1, 1, 0, 10, 0, 0, -24);} //пересечение
		else if (listS.get(i).indexOf(String.valueOf((char) 10138))>=0) {cellDate = createTextCell2(listS.get(i), higer, 1, 1, 0, 13, 0, 0, -13);} //стрелка+++
		else if (listS.get(i).indexOf(String.valueOf((char) 9008))>=0) {cellDate = createTextCell2(listS.get(i), higer, 1, 1, 0, 15, 0, 0, -15);} //две стрелки наклон+++
		else if (listS.get(i).indexOf(String.valueOf((char) 8978))>=0) {cellDate = createTextCell2(listS.get(i), higer, 1, 1, 0, 15, 0, 0, -12);} //дуга+++
		else if (listS.get(i).indexOf(String.valueOf((char) 8979))>=0) {cellDate = createTextCell2(listS.get(i), higer, 1, 1, 0, 14, 0, 0, -12);} //полуокружность+++
		else {cellDate = createTextCell2(listS.get(i), higer, 1, 0, 0, 7, 0, 0, -2);}
		dataTableDate.addCell(cellDate);
		}
		
		PdfPCell cellListDate3 = new PdfPCell(dataTableDate);
		cellListDate3.setPaddingTop(2f);
		cellListDate3.setPaddingBottom(1.5f);
		PdfPCell cellListDate2 = new PdfPCell(cellListDate3);
		cellListDate2.setBorder(Rectangle.NO_BORDER);
		return cellListDate2;
	}

	// метод для установка шрифта на кириллице, стиль
	public Font setCyrillicFont(int size, int fontStyle) {
		try {
			// Путь к шрифту "ARIALUNI"
			String fontPath = "com/kamaz/technology/apps/fonts/ARIALUNI.TTF";// "/com/kamaz/technology/apps/fonts/ARIALUNI.TTF";

			// Инициализация базового шрифта
			BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED); // Установка шрифта "ARIALUNI" как базового

			// Инициализация шрифта
			Font font = null;
			// Создание шрифта
			if (fontStyle == 0) {
				font = new Font(baseFont, size, Font.NORMAL, BaseColor.BLACK);
			} else if (fontStyle == 1) {
				font = new Font(baseFont, size, Font.BOLD, BaseColor.BLACK);
			} else if (fontStyle == 2) {
				font = new Font(baseFont, size, Font.ITALIC, BaseColor.BLACK);
			} else if (fontStyle == 3) {
				font = new Font(baseFont, size, Font.BOLDITALIC, BaseColor.BLACK);
			} else if (fontStyle == 13) {
				fontPath = "/com/kamaz/technology/apps/fonts/arialbd.ttf"; // Путь к шрифту "arialbd"
				baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED); // Установка шрифта "arialbd" как базового
				font = new Font(baseFont, size, Font.NORMAL, BaseColor.BLACK);
			}
			return font;
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// метод для установка шрифта на кириллице, стиль 2
	public float setCyrillicFontSize(String text,int size) {
		try {
			// Путь к шрифту "ARIALUNI"
			String fontPath = "com/kamaz/technology/apps/fonts/ARIALUNI.TTF";// "/com/kamaz/technology/apps/fonts/ARIALUNI.TTF";

			// Инициализация базового шрифта
			BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED); // Установка шрифта "ARIALUNI" как базового

			// Инициализация шрифта
			Font font = null;
			// Создание шрифта
				font = new Font(baseFont, size, Font.NORMAL, BaseColor.BLACK);
			 
			return font.getCalculatedBaseFont(true).getWidthPoint(text, font.getCalculatedSize());
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0f;
	}

    //метод для компановки строк по длине (с учетом поиска пробелов для переноса целых слов)
	public List<String> kompanovkaSTR(String s, int length, int left) {
		List<String> mas = new ArrayList<>();
		if (s == null || s.length() == 0) {
			return mas;
		}
		String s2 = "";
		for (int i = 0; i < s.length(); i++) {
			if (i < (s.length() - 1) && s.charAt(i) == ' ' && (s.charAt(i + 1) == '.' || s.charAt(i + 1) == ';' || s.charAt(i + 1) == ':' || s.charAt(i + 1) == ',' || s.charAt(i + 1) == '!' || s.charAt(i + 1) == '?' || s.charAt(i + 1) == ')' || s.charAt(i + 1) == '°')) {
				s2 += s.charAt(i + 1);
				i++;
			} else {
				s2 += s.charAt(i);
			}
		}
		s = s2;

		if (s.charAt(s.length() - 1) != ' ') {
			s += " ";
		}
		String words = "", word = "";
		int iP = 0, kolVo = 0;
		;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == ' ') {
				kolVo++;
				iP+=MySizeSimvole(s.charAt(i));
				if (iP < length) {
					words += word + " ";
					word = "";
				} else if (iP == length || (iP > 0 && kolVo == 1)) {
					words += word;
					word = "";
					if (left == 1) {
						words = " " + words;
					}
					mas.add(words);
					iP = 0;
					kolVo = 0;
					words = "";
				} else {
					if (left == 1) {
						words = " " + words;
					}
					mas.add(words.substring(0, words.length() - 1));
					words = word + " ";
					word = "";
					iP = MySizeString(words);
					kolVo = 1;
				}
			} else {
				word += s.charAt(i);
				iP+=MySizeSimvole(s.charAt(i));
			}
		}
		if (words.length() > 0) {
			if (left == 1) {
				words = " " + words;
			}
			mas.add(words);
		}
		return mas;
	}

    //метод для компановки списка (для разбиения длинных названий в пределах одного объекта в list) 
	public List<String> kompanovkaList(List<Revision> list, int length, String begin) {
		List<String> mas = new ArrayList<>();
		if (list == null || list.size() == 0) {
			return mas;
		}
		String rev = "", revs = "";
		int iP = 0, size = list.size() - 1, n = 0;
		;
		for (Revision r : list) {
			rev = revs;
			if (n < size) {
				revs += r.getName() + "; ";
			} else {
				revs += r.getName() + ".";
			}
			iP++;
			if (MySizeString(revs) == length) {
				mas.add(revs);
				revs = "";
				iP = 0;
			} else if (MySizeString(revs) > length && iP > 1) {
				mas.add(rev);
				revs = r.getName();
				iP = 1;
			} else if (MySizeString(revs) > length && iP == 1) {
				mas.addAll(kompanovkaSTR(revs, length, 0));
				revs = "";
				iP = 0;
			}
			n++;
		}
		if (MySizeString(revs) > 0) {
			mas.add(revs);
		}

		mas.set(0, begin + mas.get(0));
		return mas;
	}
    //Данный метод формирует List из Название КУ компанует по требуемому порядку (по ТЗ)
	public List<String> kompanovkaKu(List<Revision> kuR0) {
		List<String> str = new ArrayList<>();
		for (Revision r : kuR0) {
			str.addAll(kompanovkaSTR(r.getName(), 4340, 1));
			String ku2 = r.getIden();
			int i0 = 0, k = 0;
			String num = "", num2 = "";
			for (int i = 0; i < ku2.length(); i++) {
				if (ku2.charAt(i) == ')' && i > 0) {
					int p = i - 1;
					char c1 = ku2.charAt(p);
					num = ")";
					while (p >= 0 && (c1 == '0' || c1 == '1' || c1 == '2' || c1 == '3' || c1 == '4' || c1 == '5' || c1 == '6' || c1 == '7' || c1 == '8' || c1 == '9')) {
						num = c1 + num;
						p--;
						if (p >= 0) {
							c1 = ku2.charAt(p);
						}
					}
					if (num.length() > 1) {
						if (k > 0) {
							String kuI = "   " + num2 + ku2.substring(i0, p + 1);
							str.add(kuI);
						}
						k++;
						i0 = i + 1;
						num2 = num;
					}
				}
			}
			if (ku2.length() > i0) {
				str.add("   " + num2 + ku2.substring(i0));
			}

		}
		str.set(0, "КУ:" + str.get(0).substring(1));
		return str;
	}
	
	////Функция для компановки Строк, где требуется Надпись допусков заключить в отделью таблицу, данная функция отделяет обычный текст от правильного формата 
	////квадратных скобок [][][][sdfg], если квадратные скобки встречаются вне формата, то они приравниваются к обычному тексту
	public List<Revision> kompanovkaKubik(String str) {
		List<Revision> list = new ArrayList<>();
		boolean begin = false, close = false;
		int n = 0, k = 0, k2 = 0;
		;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '[') {
				if (i == 0 || (i > 0) && (str.charAt(i - 1) != ']' || close) && (!begin)) {
					Revision r = new Revision(str.substring(k, i), "t");
					list.add(r);
					n = i;
					begin = true;
				} else if ((i > 0) && (str.charAt(i - 1) != ']') && (begin)) {
					list.remove(list.size() - 1);
					if (k2 > n) {
						Revision r = new Revision(str.substring(k, n), "t");
						list.add(r);
						Revision r2 = new Revision(str.substring(n, k2 + 1), "d");
						list.add(r2);
						Revision r3 = new Revision(str.substring(k2 + 1, i), "t");
						list.add(r3);
						begin = false;
						k = i;
						i--;
					} else {
						Revision r = new Revision(str.substring(k, i), "t");
						list.add(r);
						n = i;
					}

				}
				close = false;
			} else if (str.charAt(i) == ']' && begin) { // &&i==(str.length()-1)||(i<(str.length()-1))&&str.charAt(i)==']'&&str.charAt(i)!='['
				if (i == (str.length() - 1) || (i < (str.length() - 1)) && str.charAt(i + 1) != '[') {
					Revision r = new Revision(str.substring(n, i + 1), "d");
					list.add(r);
					k = i + 1;
					begin = false;
					close = true;
				} else {
					k2 = i;
				}
			}
		}
		if (k < str.length()) {
			Revision r = new Revision(str.substring(k), "t");
			list.add(r);
		}
		int i=0;
		while (i<list.size()) {
			if (list.get(i).getName().length()==0) {
				list.remove(i);
			}else {i++;}
		}
		if (list.size()==0) {return list;}
		
		String teg = list.get(0).getIden();
		i=1;
		while (i<list.size()) {
			if (list.get(i).getIden().equals(teg)) {
				String name2 = list.get(i-1).getName()+list.get(i).getName();
				list.set(i-1, new Revision(teg, name2));
				list.remove(i);
			}else {
				teg=list.get(i).getIden();
				i++;
			}
		}
		list.add(new Revision("", "p"));
		return list;
	}

    //Метод для сортироваки Списка объектов по свойству name
	public List<Transition> SortTran(List<Transition> tran0) {
		List<Transition> tran1 = new ArrayList<>();
		int m = 0;
		for (int i = 0; i < tran0.size() + 6; i++) {
			for (Transition tr : tran0) {
				String num = "+"+tr.getNumber()+" "+tr.getName();
				if (tr.getNumber().equals("" + i)) {
					if (m > 0) {
						tr.setNumber("" + (i - m));
					}
					if (tr.getName().indexOf("Установ ") >= 0) {
						tr.setNumber("Y");
						m++;
					}
					tran1.add(tr);
				}
			}
		}
		return tran1;
	}

    //метод для поиска инструментов в списке с соответствующим идентификатором
	public List<Revision> sborkaInstr(String IdentiF, List<Revision> list) {
		List<Revision> newList = new ArrayList<>();
		for (Revision r : list) {
			if (IdentiF.indexOf(r.getIden()) >= 0) {
				newList.add(r);
			}
		}
		return newList;
	}

    //метод для замены фn на символ обозначающий радиус (проверка на наличие рядом цифры чтобы не хаменить естественную букву ф в слове)
	public String ReplaseФ_D(String str) {
		String str2 = "";
		if (!str.isEmpty()) {
			for (int i = 0; i < str.length() - 1; i++) {
				char c0 = str.charAt(i), c1 = str.charAt(i + 1);
				;
				if (str.substring(i, i + 1).equals("ф") && (c1 == '0' || c1 == '1' || c1 == '2' || c1 == '3' || c1 == '4' || c1 == '5' || c1 == '6' || c1 == '7' || c1 == '8' || c1 == '9')) {
					str2 += String.valueOf((char) 8960);
				} else {
					str2 += str.charAt(i);
				}
			}
			if (str.length() > 0) {
				str2 += str.charAt(str.length() - 1);
			}
		}
		return str2;
	}

    //метод для свычисления длины строки с учетом габаритов букв (W и i имею разную ширину как и все буквы) поэтому необходим метод для вычислиния точных пропорций
    //необходим для установки правильных размеров ячеек таблицы (поскольку в них нет свойства подстраиваться автоматически)
	public int MySizeSimvole(char c) {
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
	public int MySizeString(String str) {
		int length=0;
		for (int i=0; i<str.length();i++) {
			char c = str.charAt(i);
			length+=MySizeSimvole(c);
		}
		return length;
	}
}

