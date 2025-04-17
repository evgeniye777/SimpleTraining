package com.kamaz.correction_owner_allpk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.TCConstants;
import com.teamcenter.rac.kernel.IPropertyName;
import com.teamcenter.rac.kernel.ITypeName;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevisionType;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCComponentUserType;
import com.teamcenter.rac.kernel.TCComponentViewType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.UserList;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ObjectOwner;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;

public class Part {
	JTextField textField1, textField2;
	JCheckBox checkBox1, checkBox2;

	ProgressDialog progressDialog;

	public void start() {
		JButton button8 = ModificationPK("����� ����������� �������� �����������");

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 2)); // ������������� ����� 3x2

		// ������� JTextField
		textField1 = new JTextField("01.01.2024 00:00");
		textField1.setPreferredSize(new Dimension(100, 25));
		textField2 = new JTextField("01.01.2024 00:00");
		textField2.setPreferredSize(new Dimension(100, 25));

		// ������� JCheckBox
		checkBox1 = new JCheckBox("� ������        (������ '01.01.2024 00:00')");
		checkBox1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkBox1.isSelected()) {
					textField1.setEditable(false);
					textField1.setText("� ������");
				} else {
					textField1.setEditable(true);
					textField1.setText("01.01.2024 00:00");
				}
			}
		});

		checkBox2 = new JCheckBox("�� �������    (������ '01.01.2025 00:00')");
		checkBox2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkBox2.isSelected()) {
					textField2.setEditable(false);
					textField2.setText("�� �������");
				} else {
					textField2.setEditable(true);
					textField2.setText("01.01.2024 00:00");
				}
			}
		});

		panel.add(textField1);
		panel.add(checkBox1);
		panel.add(textField2);
		panel.add(checkBox2);

		JPanel buttonPanel = new JPanel(new PropertyLayout(5, 5, 10, 10, 10, 10));

		buttonPanel.add("1.1.center.center", panel);
		buttonPanel.add("2.1.center.center", button8);
		AbstractAIFDialog dialog = new AbstractAIFDialog(AIFUtility.getActiveDesktop(),
				"������ ������� �������� �������� �����������") {
			private static final long serialVersionUID = 1L;
		};
		dialog.add(buttonPanel);
		dialog.setResizable(false);
		dialog.showDialog();
	}

	private JButton ModificationPK(String string) {
		JButton button = new JButton(string);
		button.setPreferredSize(new Dimension(390, 25));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				new Thread(() -> {
					try {
						try {
							boolean s = checkBox1.isSelected(), e = checkBox2.isSelected();
							DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
							if (!s) {
								LocalDateTime ldt1 = LocalDateTime.parse(textField1.getText(), formatter);
							} // 10 ��� 2021 18:01
							if (!e) {
								LocalDateTime ldt2 = LocalDateTime.parse(textField2.getText(), formatter);
							}
							String date1 = ISformatDate(textField1.getText());
							String date2 = ISformatDate(textField2.getText());
							System.out.print("_____________����� ������:" + date1 + "  " + date2);
							if ((!s && date1 != null && date2 != null && !e) || (s && date2 != null && !e)
									|| (!s && date1 != null && e) || (s && e)) {
								starting2(string, date1, date2, s, e);
							} else {
								MessageBox.post("������� ������ ����", "������", MessageBox.ERROR);
							}
						} catch (Exception e) {
							MessageBox.post("������� ������ ����", "������", MessageBox.ERROR);
						}
					} finally {

					}
				}).start();
			}
		});

		return button;
	}

	private void starting2(String objectType, String ldt1, String ldt2, boolean dateAbsoluteStart,
			boolean dateAbsoluteEnd) {
		AIFDesktop activeDesktop = AIFUtility.getActiveDesktop();
		progressDialog = new ProgressDialog(activeDesktop, "����� ����������� �������� �����������...", 450, 80);
		progressDialog.showDialog();

		int nRevision = 0, nListForExcelFile = 0;
		AbstractAIFUIApplication currentApplication = AIFUtility.getCurrentApplication();

		TCSession session = (TCSession) currentApplication.getSession();

		String errorMessages = null;

		try {

			System.out.print("_____________����� �����");
			toSentInterFace(0, "����� �����...", -1, -1);
			TCComponentQueryType tccomponenttcquerytype = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
			TCComponentQuery query = (TCComponentQuery) tccomponenttcquerytype.find("_FindPurchased");

			TCComponent[] itemRevisions = null;
			if (!dateAbsoluteStart && !dateAbsoluteEnd) {
				itemRevisions = query.execute(new String[] { "creation_date_start", "creation_date_end" },
						new String[] { ldt1, ldt2 });
			} else if (dateAbsoluteStart && !dateAbsoluteEnd) {
				itemRevisions = query.execute(new String[] { "creation_date_end" }, new String[] { ldt2 });
			} else if (!dateAbsoluteStart && dateAbsoluteEnd) {
				itemRevisions = query.execute(new String[] { "creation_date_start" }, new String[] { ldt1 });
			} else {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
				LocalDateTime now = LocalDateTime.now();
				LocalDateTime dateAbsoluteEndPlus = now.plusDays(1);
				String formattedDate = dateAbsoluteEndPlus.format(formatter);

				ldt2 = ISformatDate(formattedDate);
				itemRevisions = query.execute(new String[] { "creation_date_end" }, new String[] { ldt2 });
			}

			System.out.print("_____________������� itemRevisions:" + itemRevisions.length);

			toSentInterFace(0, "����� �������...", -1, -1);

			TCComponentType.cacheTCPropertiesSet(itemRevisions, new String[] { "object_string", "structure_revisions",
					"creation_date", "owning_user", "owning_group" }, false);

			System.out.print("_____________����������� itemRevisions ���������");

			toSentInterFace(0, "����� ��������� �������...", -1, -1);

			TCComponent[] viewRevisions = getOwningStructureRevision(itemRevisions);

			System.out.print("_____________������� viewRevisions:" + viewRevisions.length);

			TCComponentType.cacheTCPropertiesSet(viewRevisions, new String[] { "object_string", "owning_user" }, false);

			System.out.print("_____________����������� viewRevisions ���������");

			List<DateForExcelFile> listDateForExcelFile = new ArrayList<>();

			toSentInterFace(0, "������ �������� � ��������������", -1, -1);

			progressDialog.addTablePanel();

			ObjectOwner[] objectOwners = getObjectOwners2(itemRevisions, session, listDateForExcelFile);
			nRevision = itemRevisions.length;
			toSentInterFace(0, "������ �������� � ��������������", -1, -1);
			System.out.print("_____________getObjectOwners2 ���������");

			ObjectOwner[][] objectOwnersArray = group(objectOwners);

			System.out.print("_____________group ���������");

			DataManagementService dataManagementService = DataManagementService.getService(session);

			toSentInterFace(0, "������ ���������� ������", -1, -1);

			Set<String> errorMessagesSet = new LinkedHashSet<>();

			File filePath = new File("C://Logs");
			filePath.mkdirs();

			File logFile = new File("C://Logs/" + objectType + ".log");

			write(logFile, "������	" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm")));

			for (int i = 0; i < objectOwnersArray.length; i++) {
				ObjectOwner[] objectOwners_ = objectOwnersArray[i];

				ServiceData serviceData = dataManagementService.changeOwnership(objectOwners_);

				String errorMessages_ = getErrorMessages(serviceData, listDateForExcelFile);

				errorMessagesSet.add(errorMessages_);

				write(logFile, String.join("\r\n", String.valueOf(i + 1) + "/" + //
						String.valueOf(objectOwnersArray.length) + " " + //
						LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm"))));
			}

			System.out.print("_____________changeOwnership ���������");

			write(logFile, "����� " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm")));

			errorMessages = String.join("\n", errorMessagesSet);

			nListForExcelFile = listDateForExcelFile.size();
			if (nListForExcelFile > 0) {
				// ������ ����� � EXCEL
				toSentInterFace(0, "������ ���������� � Excel...", -1, -1);
				System.out.print("������ ����� � EXCEL");

				saveDateToExcel(listDateForExcelFile);
			}

		} catch (OutOfMemoryError e) {
			progressDialog.disposeDialog();
			MessageBox.post("�� ����� ������ �������� ��������� ������������ ��������� ������", "������", MessageBox.ERROR);
        }catch (Exception e) {
        	progressDialog.disposeDialog();
			MessageBox.post("�� ��������� ������", "������", MessageBox.ERROR);
		}

		if (nRevision > 0 && nListForExcelFile > 0) {
			toSentInterFace(0, "������� ��������: " + nRevision + "/" + nRevision, -1, -1);
		} else if (nRevision > 0 && nListForExcelFile == 0) {
			toSentInterFace(0, "������� ��������: " + nRevision + "/" + nRevision + " (������ �� �������)", -1, -1);
		} else {
			progressDialog.setIndeterminate(false, 1);
			progressDialog.setValue(1);
			toSentInterFace(0, "������� ��������, ������� �� �������", -1, -1);
		}
		// progressDialog.disposeDialog();

		if (errorMessages == null) {
			MessageBox.post(activeDesktop, "����������� ������", "������", MessageBox.ERROR);
		} else if (!errorMessages.isEmpty()) {
			/*
			 * MessageBox.post(activeDesktop, "�������� ������", new
			 * JScrollPane(createTextArea(errorMessages, false)), "������",
			 * MessageBox.ERROR);
			 */
		} else {
			// MessageBox.post(activeDesktop, "������", "����������",
			// MessageBox.INFORMATION);
		}
	}

	private void write(File logFile, String text) throws IOException {
		try (FileWriter fileWriter = new FileWriter(logFile, true)) {
			fileWriter.write(text + "\r\n");
		}
	}

	public ObjectOwner[][] group(ObjectOwner[] objectOwners) {
		List<List<ObjectOwner>> objectOwnerListList = new ArrayList<>();

		for (int i = 0; i < objectOwners.length; i++) {
			ObjectOwner objectOwner = objectOwners[i];

			if (i % 1000 == 0) {
				List<ObjectOwner> objectOwnerList = new ArrayList<>();
				objectOwnerList.add(objectOwner);

				objectOwnerListList.add(objectOwnerList);
			} else {
				List<ObjectOwner> objectOwnerList = objectOwnerListList.get(objectOwnerListList.size() - 1);

				objectOwnerList.add(objectOwner);
			}
		}

		List<ObjectOwner[]> objectOwnerList = new ArrayList<>();

		for (List<ObjectOwner> objectOwnerList_ : objectOwnerListList) {
			ObjectOwner[] objectOwners_ = objectOwnerList_.toArray(new ObjectOwner[objectOwnerList_.size()]);

			objectOwnerList.add(objectOwners_);
		}

		ObjectOwner[][] objectOwnersArray = objectOwnerList.toArray(new ObjectOwner[objectOwnerList.size()][]);

		return objectOwnersArray;
	}

	private String getErrorMessages(ServiceData serviceData) {
		Set<String> errorMessageSet = new LinkedHashSet<>();

		for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
			ErrorStack partialError = serviceData.getPartialError(i);

			ErrorValue[] errorValues = partialError.getErrorValues();

			for (ErrorValue errorValue : errorValues) {
				String message = errorValue.getMessage();

				errorMessageSet.add(message);
			}
		}

		String errorMessages = String.join("\n", errorMessageSet);

		return errorMessages;
	}

	private String getErrorMessages(ServiceData serviceData, List<DateForExcelFile> listDateForExcelFile) {
		Set<String> errorMessageSet = new LinkedHashSet<>();

		for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
			ErrorStack partialError = serviceData.getPartialError(i);

			ErrorValue[] errorValues = partialError.getErrorValues();

			String myError = "";
			for (ErrorValue errorValue : errorValues) {
				String message = errorValue.getMessage();
				errorMessageSet.add(message);
				myError += message + "; ";
			}
			myError = myError.substring(0, myError.length() - 2);

			for (DateForExcelFile dForExcelFile : listDateForExcelFile) {
				int iIdRev = dForExcelFile.getPk().indexOf(";");
				if (iIdRev < 0) {
					iIdRev = dForExcelFile.getPk().length();
				}
				if (dForExcelFile.getPk() != null && myError.contains(dForExcelFile.getPk().substring(0, iIdRev))
						|| dForExcelFile.getStrukt() != null && myError.contains(dForExcelFile.getStrukt())) {
					dForExcelFile.setError("������ ��� ������ � TeamCenter: " + myError);
					dForExcelFile.setAction("������� ���������: '" + dForExcelFile.getAction() + "'");
				}
			}

		}

		String errorMessages = String.join("\n", errorMessageSet);

		return errorMessages;
	}

	private JTextArea createTextArea(String text, boolean editable) {
		JTextArea textArea = new JTextArea(text);
		textArea.setEditable(editable);

		return textArea;
	}

	private ObjectOwner[] getObjectOwners(TCComponentUser user, TCComponentGroup group, TCComponent[] items)
			throws TCException {
		List<ObjectOwner> objectOwnerList = new ArrayList<>();

		for (TCComponent item : items) {
			TCComponentForm masterForm = (TCComponentForm) item.getRelatedComponent("IMAN_master_form");
			TCComponent[] views = item.getRelatedComponents("bom_view_tags");
			TCComponent[] itemRevisions = item.getRelatedComponents("revision_list");

			add(objectOwnerList, createObjectOwner(user, group, item));
			add(objectOwnerList, createObjectOwner(user, group, masterForm));
			add(objectOwnerList, createObjectOwners(user, group, views));

			for (TCComponent itemRevision : itemRevisions) {
				TCComponentForm masterFormRevision = (TCComponentForm) itemRevision
						.getRelatedComponent("IMAN_master_form_rev");
				TCComponent[] viewRevisions = itemRevision.getRelatedComponents("structure_revisions");
				TCComponent[] factoryForms = itemRevision.getRelatedComponents("K7_rlZavodForm");
				TCComponent[] sapForms = itemRevision.getRelatedComponents("K7_rlSAP");
				TCComponent[] etalonSapForms = itemRevision.getRelatedComponents("K7_rlEtSAP");

				add(objectOwnerList, createObjectOwner(user, group, itemRevision));
				add(objectOwnerList, createObjectOwner(user, group, masterFormRevision));
				add(objectOwnerList, createObjectOwners(user, group, viewRevisions));
				add(objectOwnerList, createObjectOwners(user, group, factoryForms));
				add(objectOwnerList, createObjectOwners(user, group, sapForms));
				add(objectOwnerList, createObjectOwners(user, group, etalonSapForms));
			}
		}

		ObjectOwner[] objectOwners = objectOwnerList.toArray(new ObjectOwner[objectOwnerList.size()]);

		return objectOwners;
	}

	private ObjectOwner[] getObjectOwners2(TCComponent[] itemRevisions, TCSession session,
			List<DateForExcelFile> listDateForExcelFile) throws TCException {
		System.out.print("������ �������� � ��������������");

		List<ObjectOwner> objectOwnerList = new ArrayList<>();

		TCComponentUserType userType = (TCComponentUserType) session.getTypeComponent("User");
		TCComponentGroupType groupType = (TCComponentGroupType) session.getTypeComponent("Group"); 

		int iNullStukture = 0, iErrorOwnerStructure = 0, iErrorOwnerRevision = 0, iAnotherError = 0, AllRevision = 0,
				n = itemRevisions.length;

		progressDialog.setIndeterminate(false, n);
		progressDialog.setValue(0);
		for (TCComponent itemRevision : itemRevisions) {
			// ����������� ������ � ����������
			AllRevision++;
			toSentInterFace(0, "������� ����������: " + AllRevision + "/" + n, -1, -1);
			progressDialog.setValue(AllRevision);

			UserInformation uInformation = new UserInformation();
			DateForExcelFile dForExcelFile = new DateForExcelFile();
			String owner_rev_full = null, name_rev = null;
			try {
				name_rev = itemRevision.getProperty("object_string"); // object_name
				dForExcelFile.setPk(name_rev);
				owner_rev_full = itemRevision.getProperty("owning_user");
				uInformation = getBOMViewUser(owner_rev_full, userType, groupType, session,
						itemRevision.getProperty("owning_group"));
			} catch (Exception e) {
				uInformation.setNumberResult(0);
				uInformation.setError(
						"�� ������� ���������� ������ �������, ������ �� ��������, ��� ������: '" + e.toString() + "'");
				// ������
				// ���������� ������ excel
				dForExcelFile.setAction("�������� �� ���������");
				dForExcelFile.setError(uInformation.getError());
				listDateForExcelFile.add(dForExcelFile);
				// ����������� ������ � ����������
				iAnotherError++;
				toSentInterFace(1, "" + iAnotherError, 1, 3);
				continue;
			}
			if (uInformation.getNumberResult()==0) {
				if (uInformation.getError()==null) {uInformation.setError("�� ��������� ������");}
				// ������
				// ���������� ������ excel
				dForExcelFile.setAction("�������� �� ���������");
				dForExcelFile.setError(uInformation.getError());
				listDateForExcelFile.add(dForExcelFile);
				// ����������� ������ � ����������
				iAnotherError++;
				toSentInterFace(1, "" + iAnotherError, 1, 3);
				continue;
			}
			TCComponentGroup group = null;
			try {
				if (uInformation.getGroup() == null) {
					group = groupType.find(uInformation.getUser().getProperty(TCComponentUser.PROP_LOGIN_GROUP));
					uInformation.setGroup(group);
					if (group==null) {
						throw new Exception("�������� owning_group �� ������");}
				} 
			} catch (Exception e) {
				dForExcelFile.setAction("�������� �� ���������");
				if (uInformation.getGroupStr()==null) {uInformation.setGroupStr("������ �����");}
				dForExcelFile.setError("�� ������� ���������� ������ ���������, ��� ������: '" + e.toString()
						+ "' ����������� ������: '" + uInformation.getGroupStr() + "'");
				listDateForExcelFile.add(dForExcelFile);
				// ����������� ������ � ����������
				iAnotherError++;
				toSentInterFace(1, "" + iAnotherError, 1, 3);
				continue;
			}

			if (uInformation.getNumberResult() == 1) {

				add(objectOwnerList, createObjectOwner(uInformation.getUser(), group, itemRevision));
				// ���������� ������ excel
				dForExcelFile.setAction("�������������� ��������� �������");
				dForExcelFile.setOldOwner(owner_rev_full);
				String owner_rev_full_new = uInformation.getUser().getProperty("object_string");
				dForExcelFile.setNewOlder(owner_rev_full_new);
				listDateForExcelFile.add(dForExcelFile);
				// ����������� ������ � ����������
				iErrorOwnerRevision++;
				toSentInterFace(1, "" + iErrorOwnerRevision, 1, 2);

				owner_rev_full = owner_rev_full_new;
				dForExcelFile = new DateForExcelFile();
				dForExcelFile.setPk(name_rev);
				StructureInformation sInformation = getBOMViewRevision((TCComponentItemRevision) itemRevision, "View",
						true, false);
				if (sInformation.getNameStruct() != null) {
					dForExcelFile.setStrukt(sInformation.getNameStruct());
				}
				if (sInformation.getNumberResult() == 1) {
					add(objectOwnerList, createObjectOwner(uInformation.getUser(), group, sInformation.getResult()));
					// ���������� ������ excel
					dForExcelFile.setAction("�������� �������");
					dForExcelFile.setNewOlder(owner_rev_full);
					listDateForExcelFile.add(dForExcelFile);
					// ����������� ������ � ����������
					iNullStukture++;
					toSentInterFace(1, "" + iNullStukture, 1, 0);

					dForExcelFile = new DateForExcelFile();
					dForExcelFile.setPk(name_rev);
				} else if (sInformation.getNumberResult() == 2) {
					String owner_struct_full = sInformation.getResult().getProperty("owning_user");
					if (!owner_rev_full.equals(owner_struct_full)) {
						add(objectOwnerList,
								createObjectOwner(uInformation.getUser(), group, sInformation.getResult()));
						// ���������� ������ excel
						dForExcelFile.setAction("�������������� ��������� �������");
						dForExcelFile.setOldOwner(owner_struct_full);
						dForExcelFile.setNewOlder(owner_rev_full);
						listDateForExcelFile.add(dForExcelFile);
						// ����������� ������ � ����������
						iErrorOwnerStructure++;
						toSentInterFace(1, "" + iErrorOwnerStructure, 1, 1);

						dForExcelFile = new DateForExcelFile();
						dForExcelFile.setPk(name_rev);
					}
				} else { // ������
							// ���������� ������ excel
					dForExcelFile.setAction("�������� �� ���������");
					dForExcelFile.setError(sInformation.getError());
					listDateForExcelFile.add(dForExcelFile);
					// ����������� ������ � ����������
					iAnotherError++;
					toSentInterFace(1, "" + iAnotherError, 1, 3);

					dForExcelFile = new DateForExcelFile();
					dForExcelFile.setPk(name_rev);
				}
			} else if (uInformation.getNumberResult() == 2) {
				TCComponentItemRevision qwer = (TCComponentItemRevision) itemRevision;
				StructureInformation sInformation = getBOMViewRevision((TCComponentItemRevision) itemRevision, "View",
						true, false);
				if (sInformation.getNameStruct() != null) {
					dForExcelFile.setStrukt(sInformation.getNameStruct());
				}
				if (sInformation.getNumberResult() == 1) {
					add(objectOwnerList, createObjectOwner(uInformation.getUser(), group, sInformation.getResult()));
					// ���������� ������ excel
					dForExcelFile.setAction("�������� �������");
					dForExcelFile.setNewOlder(owner_rev_full);
					listDateForExcelFile.add(dForExcelFile);
					// ����������� ������ � ����������
					iNullStukture++;
					toSentInterFace(1, "" + iNullStukture, 1, 0);

					dForExcelFile = new DateForExcelFile();
					dForExcelFile.setPk(name_rev);
				} else if (sInformation.getNumberResult() == 2) {
					String owner_struct_full = sInformation.getResult().getProperty("owning_user");
					if (!owner_rev_full.equals(owner_struct_full)) {
						add(objectOwnerList,
								createObjectOwner(uInformation.getUser(), group, sInformation.getResult()));
						// ���������� ������ excel
						dForExcelFile.setAction("�������������� ��������� �������");
						dForExcelFile.setOldOwner(owner_struct_full);
						dForExcelFile.setNewOlder(owner_rev_full);
						listDateForExcelFile.add(dForExcelFile);
						// ����������� ������ � ����������
						iErrorOwnerStructure++;
						toSentInterFace(1, "" + iErrorOwnerStructure, 1, 1);

						dForExcelFile = new DateForExcelFile();
						dForExcelFile.setPk(name_rev);
					}
				} else { // ������
							// ���������� ������ excel
					dForExcelFile.setAction("�������� �� ���������");
					dForExcelFile.setError(sInformation.getError());
					listDateForExcelFile.add(dForExcelFile);
					// ����������� ������ � ����������
					iAnotherError++;
					toSentInterFace(1, "" + iAnotherError, 1, 3);

					dForExcelFile = new DateForExcelFile();
					dForExcelFile.setPk(name_rev);
				}
			} else { // ������
						// ���������� ������ excel
				dForExcelFile.setAction("�������� �� ���������");
				dForExcelFile.setError(uInformation.getError());
				listDateForExcelFile.add(dForExcelFile);
				// ����������� ������ � ����������
				iAnotherError++;
				toSentInterFace(1, "" + iAnotherError, 1, 3);

				dForExcelFile = new DateForExcelFile();
				dForExcelFile.setPk(name_rev);
			}

		}

		ObjectOwner[] objectOwners = objectOwnerList.toArray(new ObjectOwner[objectOwnerList.size()]);
		return objectOwners;
	}

	private void add(List<ObjectOwner> objectOwnerList, ObjectOwner[] objectOwners) {
		if (objectOwners != null && objectOwners.length != 0) {
			for (ObjectOwner objectOwner : objectOwners) {
				objectOwnerList.add(objectOwner);
			}
		}
	}

	private void add(List<ObjectOwner> objectOwnerList, ObjectOwner objectOwner) {
		if (objectOwner != null) {
			objectOwnerList.add(objectOwner);
		}
	}

	private TCComponent[] getItemRevisions(TCComponent[] items) throws TCException {
		List<TCComponent> itemRevisionList = new ArrayList<>();

		for (TCComponent item : items) {
			TCComponent[] itemRevisions = item.getRelatedComponents("revision_list");

			for (TCComponent itemRevision : itemRevisions) {
				itemRevisionList.add(itemRevision);
			}
		}

		TCComponent[] itemRevisions = itemRevisionList.toArray(new TCComponent[itemRevisionList.size()]);
		return itemRevisions;
	}

	private TCComponent[] getOwningStructureRevision(TCComponent[] itemRevisions) throws TCException {
		List<TCComponent> itemRevisionList = new ArrayList<>();

		for (TCComponent itemRevision : itemRevisions) {
			TCComponent[] viewRevisions = itemRevision.getRelatedComponents("structure_revisions");
			if (viewRevisions.length > 0) {
				itemRevisionList.add(viewRevisions[0]);
			}
		}

		TCComponent[] viewRevisionsMas = itemRevisionList.toArray(new TCComponent[itemRevisionList.size()]);
		return viewRevisionsMas;
	}

	private ObjectOwner[] createObjectOwners(TCComponentUser user, TCComponentGroup group, TCComponent[] components) {
		List<ObjectOwner> objectOwnerList = new ArrayList<>();

		if (components != null && components.length != 0) {
			for (TCComponent component : components) {
				ObjectOwner objectOwner = createObjectOwner(user, group, component);

				objectOwnerList.add(objectOwner);
			}
		}

		ObjectOwner[] objectOwners = objectOwnerList.toArray(new ObjectOwner[objectOwnerList.size()]);

		return objectOwners;
	}

	private ObjectOwner createObjectOwner(TCComponentUser user, TCComponentGroup group, TCComponent component) {
		if (component != null) {
			ObjectOwner componentObjectOwner = new ObjectOwner();
			componentObjectOwner.object = component;
			componentObjectOwner.owner = user;
			componentObjectOwner.group = group;

			return componentObjectOwner;
		}

		return null;
	}

	public static String[] getDateRange(String startDate, String endDate) {
		String[] masMonth = new String[] { "���", "���", "���", "���", "���", "���", "���", "���", "���", "���", "���",
				"���" };

		// ������ ���� �� �����
		LocalDate start = LocalDate.parse(startDate);
		LocalDate end = LocalDate.parse(endDate);
		end = end.plusDays(1);

		// ������� ������ ��� �������� ���
		List<String> dateList = new ArrayList<>();

		// ��������� ������ ������ �� �����
		for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
			String dateStr = "" + date.getDayOfMonth() + " " + masMonth[date.getMonthValue() - 1] + " " + date.getYear()
					+ " 00:00";
			dateList.add(dateStr);
		}

		// ����������� ������ � ������ �����
		return dateList.toArray(new String[0]);
	}

	public String formatDate(String date) {
		String newDate = "";
		try {
			String[] masData = date.split(" ");
			String[] masMonth = new String[] { "���", "���", "���", "���", "���", "���", "���", "���", "���", "���",
					"���", "���" };

			newDate += masData[2];
			int i = 0;
			for (String month : masMonth) {
				i++;
				if (masData[1].equals(month)) {
					if (i > 9) {
						newDate += "-" + i;
					} else {
						newDate += "-0" + i;
					}
					break;
				}
			}
			newDate += "-" + masData[0];
		} catch (Exception e) {
			newDate = null;
		}
		try {
			String[] masProv = newDate.split("-");
			if (masProv.length == 3) {
				Integer.parseInt(masProv[0]);
				Integer.parseInt(masProv[1]);
				Integer.parseInt(masProv[2]);
			} else {
				newDate = null;
			}
		} catch (Exception e) {
			newDate = null;
		}
		return newDate;
	}

	public String ISformatDate(String dateTime) {
		String newDate = "";
		try {
			String[] masDataTime = dateTime.split(" ");
			String date = masDataTime[0];
			String[] masData = date.split("\\.");
			String[] masMonth = new String[] { "���", "���", "���", "���", "���", "���", "���", "���", "���", "���",
					"���", "���" };
			if (masData[0].length() == 1) {
				newDate += "0";
			}
			newDate += masData[0] + " ";
			int iMonth = Integer.parseInt(masData[1]) - 1;
			if (iMonth < 0) {
				return null;
			}
			newDate += masMonth[iMonth] + " ";
			newDate += masData[2] + " " + masDataTime[1];
		} catch (Exception e) {
			newDate = null;
		}
		return newDate;
	}

	public String formatName(String name) {
		Pattern pattern = Pattern.compile("\\((.*?)\\)");
		Matcher matcher = pattern.matcher(name);

		if (matcher.find()) {
			String content = matcher.group(1);
			return content;
		} else {
			return name;
		}
	}

	public void createTextFile(String content, String name) {
		// �������� ���� � �������� �����
		String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
		// ��� �����
		String fileName = name + ".txt";
		// ������ ���� � �����
		File file = new File(desktopPath, fileName);

		// ���������� ������ � ����
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(content);
			System.out.println("���� ������� ������ � ������ ��������.");
		} catch (IOException e) {
			System.err.println("��������� ������ ��� �������� �����: " + e.getMessage());
		}
	}

	/**
	 * ����� ���������� ������������ ��� ������ ����� BOMViewRevision (��������� �
	 * ��������� ������� <b><code>viewtype</code></b> ����) ��� ������� �������,
	 * ���������� � ��������� ������� <br>
	 * ����� ���������� <b><code>null</code></b> � ������ ���� ��������� ������ ���
	 * �������� ��� ������� BOMViewRevision ������ BOMViewRevision (����
	 * ������������)
	 *
	 * @param revision
	 * @param viewtype       - ��� BOMViewRevision (��� ���� �������� ���� "View")
	 * @param create_if_null - ��������� BOMViewRevision ���� ����������� � �������
	 *                       (��� ���� �������� ���� true)
	 * @param isPrecise      - �������� (��� ���� �������� ���� false)
	 * @return {@link String} - ������ ��� ���������� � ��������� ������� �������
	 *         ���� ������� ������ �� ����������
	 */
	public static StructureInformation getBOMViewRevision(TCComponentItemRevision revision, String viewtype,
			boolean create_if_null, boolean isPrecise) {
		String error = "";
		TCComponentBOMViewRevision result = null;
		StructureInformation sInformation = new StructureInformation();
		try {
			TCProperty prop = revision.getTCProperty(TCConstants.CLASS_Bom_View_Rev);
			for (TCComponent component : prop.getReferenceValueArray()) {
				TCComponent bomview = component.getReferenceProperty(IPropertyName.BOM_VIEW);
				TCComponent vt = bomview.getReferenceProperty(IPropertyName.VIEW_TYPE);
				if (vt.getTCProperty("name").getStringValue().equalsIgnoreCase(viewtype)) {
					result = (TCComponentBOMViewRevision) component;
					sInformation.setResult(result);
					sInformation.setNameStruct(component.getProperty("object_string"));
					sInformation.setNumberResult(2);
					break;
				}
			}
			if (result == null && create_if_null) {
				// �������� ������ BOMViewRevision ��� �������, ���������� �
				// ��������� �������
				TCSession session = revision.getSession();
				TCComponentViewType viewTypeComp = null;
				TCComponentType typeComp = session.getTypeComponent(ITypeName.PSViewType);
				if (typeComp != null) {
					TCComponent[] viewTypeComps = typeComp.extent();
					if (viewTypeComps != null) {
						for (TCComponent viewTypeComp2 : viewTypeComps) {
							if (viewTypeComp2.getTCProperty("name").getStringValue().equalsIgnoreCase(viewtype)) {
								viewTypeComp = (TCComponentViewType) viewTypeComp2;
								break;
							}
						}
					}
				}
				if (viewTypeComp != null) {
					TCComponentBOMViewRevisionType bvrType = (TCComponentBOMViewRevisionType) session
							.getTypeComponent("PSBOMViewRevision");
					String itemID = revision.getProperty(IPropertyName.ITEM_ID);
					String revID = revision.getProperty(IPropertyName.ITEM_REVISION_ID);
					result = bvrType.create(itemID, revID, viewTypeComp, isPrecise);
					sInformation.setResult(result);
					sInformation.setNameStruct(result.getProperty("object_string"));
					sInformation.setNumberResult(1);
				}
			}
		} catch (TCException e) {
			error = e.toString();
			sInformation.setError(error);
			sInformation.setNumberResult(0);
		}
		return sInformation;
	}

	private UserList getUserByName(String userName) throws TCException {
		TCSession session = (TCSession) AIFUtility.getDefaultSession();

		TCComponentUserType userType = (TCComponentUserType) session.getTypeComponent("User");

		UserList userList = userType.getUserListByUser(userName);

		// String[] usersSTR = userList.getUserNames(); //.getUserComponentAtIndex(0)

		return userList;
	}

	private UserInformation getBOMViewUser(String owner_rev_full, TCComponentUserType userType,
			TCComponentGroupType groupType, TCSession session, String groupRevision) {
		UserInformation uInformation = new UserInformation();
		String owner_rev = formatName(owner_rev_full); /// ������� ������ ����� ������������, ������� � ������� �������
		try {
			TCComponentUser owning_user = userType.find(owner_rev);
			uInformation.setUser(owning_user);
			uInformation.setNumberResult(2);
		} catch (TCException e) {
			uInformation.setNumberResult(0);
			uInformation.setError(
					"�� ������� ���������� ������ ���������, ������ ��������� � ������� �� ��������, ��� ������: '"
							+ e.toString() + "'");
		}
		if (uInformation.getNumberResult() == 2) {
			try {
				String personUser = uInformation.getUser().getProperty(TCComponentUser.PROP_PERSON);

				String statusUser = uInformation.getUser().getProperty(TCComponentUser.PROP_USER_STATUS);

				String groupUser;
				try {
					groupUser = uInformation.getUser().getProperty(TCComponentUser.PROP_LOGIN_GROUP);
				} catch (Exception e) {
					groupUser = null;
				}

				if (!statusUser.equals("0")) {
					uInformation = searchTheBestUser(personUser, groupUser, groupType, session, groupRevision);
				}
			} catch (TCException e) {
				uInformation.setError(
						"�� ������� ���������� ���������� ������������, ������ ��������� � ������� �� ��������, ��� ������: '"
								+ e.toString() + "'");
			}
		}
		return uInformation;
	}

	private UserInformation searchTheBestUser(String personUser, String groupUser, TCComponentGroupType groupType,
			TCSession session, String groupRevision) {
		UserInformation uInformation = new UserInformation();
		TCComponentUser bestUser = null;
		TCComponentGroup group = null;
		boolean rStatus = false;
		UserList userList = null;
		String groupSTR = null;
		try {
			userList = getUserByName(personUser);
			String[] usersSTR = userList.getUserNames(); //
			if (usersSTR.length > 0) {
				if (usersSTR.length == 1) {
					bestUser = userList.getUserComponentAtIndex(0);
					String statusUser = bestUser.getProperty(TCComponentUser.PROP_USER_STATUS);
					if (statusUser.equals("0")) {
						rStatus = true;
					}
				} else {
					TCComponentUser bestOfBestUser = null;
					;
					for (int i = 0; i < usersSTR.length; i++) {
						bestOfBestUser = userList.getUserComponentAtIndex(i);
						String statusUser = bestOfBestUser.getProperty(TCComponentUser.PROP_USER_STATUS);
						if (statusUser.equals("0")) {
							rStatus = true;
							bestUser = bestOfBestUser;
							break;
						}
					}
				}
			}
			if (!rStatus) {
				
				if (groupUser != null) {
					group = groupType.find(groupUser);
					groupSTR = groupUser;
				} else if (groupRevision != null) {
					group = groupType.find(groupRevision);
					groupSTR = groupRevision;
				}
				if (group != null) {
					TCComponentRole[] roles = group.getRoles();
					for (TCComponentRole role : roles) {// TCComponentRole role = session.getCurrentRole();

						TCComponentUser[] users = role.getUsers(group);

						TCComponentUser bestOfBestUser = null;

						for (int i = 0; i < users.length; i++) {
							bestOfBestUser = users[i];
							String statusUser = bestOfBestUser.getProperty(TCComponentUser.PROP_USER_STATUS);
							if (statusUser.equals("0")) {
								rStatus = true;
								bestUser = bestOfBestUser;
								break;
							}
						}
						if (rStatus) {
							break;
						}
					}
				}
			}

			if (rStatus) {
				uInformation.setNumberResult(1);
				uInformation.setUser(bestUser);
				uInformation.setGroupStr(groupSTR);
				if (group!=null) {}
					uInformation.setGroup(group);
			} else {
				uInformation.setNumberResult(0);
				uInformation.setError("��������� ��� ������, � ����������� ��������� �� �������");
			}
		} catch (TCException e) {
			uInformation.setNumberResult(0);
			uInformation.setError("�� ������� ����� ������ ���������, ��� ������ '" + e.toString() + "'");
		}
		return uInformation;
	}

	private void saveDateToExcel(List<DateForExcelFile> listDateForExcelFile) {
		String nameFile = "����� ( � " + textField1.getText() + " �� " + textField2.getText() + " ).xlsx";
		nameFile = nameFile.replaceAll("[\\\\/:*?\"<>|]", "_");
		String path = "D:\\MyFiles\\";
		File file = new File(path + nameFile);
		Workbook book = new XSSFWorkbook();
		Sheet sheet = book.createSheet("�����");

		Font font = book.createFont();
		font.setColor(IndexedColors.BLACK.getIndex());
		font.setFontHeightInPoints((short) 14);

		CellStyle styleError = book.createCellStyle(); // ����� ������
		styleError.setFillForegroundColor(IndexedColors.RED.getIndex());
		styleError.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleError.setFont(font);
		styleError.setBorderTop(BorderStyle.MEDIUM);
		styleError.setBorderBottom(BorderStyle.MEDIUM);
		styleError.setBorderLeft(BorderStyle.MEDIUM);
		styleError.setBorderRight(BorderStyle.MEDIUM);

		CellStyle styleErrorWrite = book.createCellStyle(); // ����� ������2
		styleErrorWrite.setFillForegroundColor(IndexedColors.CORAL.getIndex());
		styleErrorWrite.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleErrorWrite.setFont(font);
		styleErrorWrite.setBorderTop(BorderStyle.MEDIUM);
		styleErrorWrite.setBorderBottom(BorderStyle.MEDIUM);
		styleErrorWrite.setBorderLeft(BorderStyle.MEDIUM);
		styleErrorWrite.setBorderRight(BorderStyle.MEDIUM);

		CellStyle styleSuccess = book.createCellStyle(); // ����� ��������� ����������
		styleSuccess.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		styleSuccess.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleSuccess.setFont(font);
		styleSuccess.setBorderTop(BorderStyle.MEDIUM);
		styleSuccess.setBorderBottom(BorderStyle.MEDIUM);
		styleSuccess.setBorderLeft(BorderStyle.MEDIUM);
		styleSuccess.setBorderRight(BorderStyle.MEDIUM);

		CellStyle styleEmpty = book.createCellStyle(); // ����� �������
		styleEmpty.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		styleEmpty.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleEmpty.setFont(font);
		styleEmpty.setBorderTop(BorderStyle.MEDIUM);
		styleEmpty.setBorderBottom(BorderStyle.MEDIUM);
		styleEmpty.setBorderLeft(BorderStyle.MEDIUM);
		styleEmpty.setBorderRight(BorderStyle.MEDIUM);

		CellStyle styleEmptyHeading = book.createCellStyle(); // ����� �������
		styleEmpty.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		styleEmpty.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleEmpty.setFont(font);
		styleEmpty.setBorderTop(BorderStyle.MEDIUM);
		styleEmpty.setBorderBottom(BorderStyle.MEDIUM);
		styleEmpty.setBorderLeft(BorderStyle.MEDIUM);
		styleEmpty.setBorderRight(BorderStyle.MEDIUM);
		styleEmptyHeading.setAlignment(HorizontalAlignment.CENTER);

		Row row = sheet.createRow(0);
		row.setHeight((short) (22 * 22));

		Cell cell0 = row.createCell(0);
		Cell cell1 = row.createCell(1);
		Cell cell2 = row.createCell(2);
		Cell cell3 = row.createCell(3);
		Cell cell4 = row.createCell(4);
		Cell cell5 = row.createCell(5);

		cell0.setCellValue("�������� ���������");
		cell1.setCellValue("��������");
		cell2.setCellValue("������ ��������");
		cell3.setCellValue("����� ��������");
		cell4.setCellValue("������");
		cell5.setCellValue("");
		cell0.setCellStyle(styleEmpty);
		cell1.setCellStyle(styleEmpty);
		cell2.setCellStyle(styleEmpty);
		cell3.setCellStyle(styleEmpty);
		cell4.setCellStyle(styleEmpty);

		for (int i = 0; i < listDateForExcelFile.size(); i++) {
			DateForExcelFile dForExcelFile = listDateForExcelFile.get(i);
			row = sheet.createRow(i + 1);
			row.setHeight((short) (22 * 22));

			cell0 = row.createCell(0);
			cell1 = row.createCell(1);
			cell2 = row.createCell(2);
			cell3 = row.createCell(3);
			cell4 = row.createCell(4);
			cell5 = row.createCell(5);
			if (dForExcelFile.getPk() != null) {
				cell0.setCellStyle(styleEmpty);
				cell0.setCellValue(dForExcelFile.getPk());
			} else {
				cell0.setCellValue("Error_Name");
				cell0.setCellStyle(styleError);
			}

			if (dForExcelFile.getAction() != null) {
				row.createCell(1).setCellValue(dForExcelFile.getAction());
				if (dForExcelFile.getAction().startsWith("������� ���������:")
						|| dForExcelFile.getAction().startsWith("�������� �� ���������")) {
					cell1.setCellStyle(styleErrorWrite);
				} else {
					cell1.setCellStyle(styleSuccess);
				}
			} else {
				cell1.setCellValue("�������� �� ���������");
				cell1.setCellStyle(styleErrorWrite);
			}

			if (dForExcelFile.getOldOwner() != null) {
				cell2.setCellValue(dForExcelFile.getOldOwner());
				cell2.setCellStyle(styleEmpty);
			} else {
				cell2.setCellStyle(styleEmpty);
			}

			if (dForExcelFile.getNewOlder() != null) {
				cell3.setCellValue(dForExcelFile.getNewOlder());
				cell3.setCellStyle(styleEmpty);
			} else {
				cell3.setCellStyle(styleEmpty);
			}

			if (dForExcelFile.getError() != null) {
				cell4.setCellValue(dForExcelFile.getError());
				cell4.setCellStyle(styleError);
			} else {
				cell4.setCellStyle(styleEmpty);
			}
			cell5.setCellValue("");
		}
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.setColumnWidth(4, 12000);

		try {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("��������� ���� ������");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				file = fileChooser.getSelectedFile();
				String selectedPath = file.getAbsolutePath();
				if (selectedPath.endsWith(".xlsx") && selectedPath.length() > 5) {
					selectedPath = selectedPath.substring(0, selectedPath.length() - 5);
				}
				// Path documentsPath = Paths.get(selectedPath, nameFile);*/
				file = new File(selectedPath + ".xlsx");
				book.write(new FileOutputStream(file));
			} else {
				String userHome = System.getProperty("user.home");
				Path documentsPath = Paths.get(userHome, "Documents//" + nameFile);
				book.write(new FileOutputStream(documentsPath.toString()));
				MessageBox.post("���� �������� � : '" + documentsPath.toString() + "'", "", MessageBox.INFORMATION);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void toSentInterFace(int type, String text, int x, int y) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (type == 0) {
					progressDialog.updateLabelMessage(text);
				} else if (type == 1) {
					progressDialog.editCell(y, x, text);
				}
			}
		});
	}

}

class ProgressDialog extends AbstractAIFDialog {
	private static final long serialVersionUID = 1L;

	private int width, height;

	DefaultTableModel model;

	JPanel panelTabel;

	JLabel labelMessage;

	JProgressBar progressBar;

	public ProgressDialog(AIFDesktop aifDesktop, String title, int width, int height) {
		super(aifDesktop, title);

		this.width = width;
		this.height = height;
	}

	@Override
	public void showDialog() {
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);

		labelMessage = new JLabel("������ ����������");
		labelMessage.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 18));

		panelTabel = createTablePanel();
		panelTabel.setVisible(false);

		this.add(progressBar, "North");
		this.add(panelTabel);
		this.add(labelMessage, "South");
		// height =
		this.setPreferredSize(new Dimension(width, height));
		this.pack();
		this.validate();
		this.centerToScreen();
		this.setVisible(true);
	}

	private JPanel createTablePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		// ������ ��� �������
		String[] columnNames = { "��� ������������� ������", "���-��" };
		Object[][] data = { { "���������� ������� � �������", "0" },
				{ "�������������� ���������� ������� � �������", "0" }, { "���������� ��������", "0" },
				{ "������ ������", "0" } };

		// ������� ������ �������
		model = new DefaultTableModel(data, columnNames);
		JTable table = new JTable(model);
		table.setRowHeight(30);

		// ��������� ������� � JScrollPane
		JScrollPane scrollPane = new JScrollPane(table);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	public void editCell(int rowIndex, int columnIndex, Object newValue) {
		if (rowIndex >= 0 && rowIndex < model.getRowCount() && columnIndex >= 0
				&& columnIndex < model.getColumnCount()) {
			model.setValueAt(newValue, rowIndex, columnIndex);
		} else {
			System.out.println("������� ��� ���������");
		}
	}

	public void addTablePanel() {
		this.setSize(new Dimension(width, height + 146));
		this.revalidate();
		this.repaint();
		panelTabel.setVisible(true);
	}

	public void updateLabelMessage(String newMessage) {
		labelMessage.setText(newMessage);
		labelMessage.revalidate(); // ��������� �����
		labelMessage.repaint(); // �������������� �����
	}

	public void setIndeterminate(boolean interminate, int max) {
		progressBar.setIndeterminate(interminate);
		if (!interminate) {
			progressBar.setMaximum(max);
		} else {
			progressBar.setValue(max);
		}
	}

	public void setValue(int value) {
		progressBar.setValue(value);
	}
}

class StructureInformation {
	public String getNameStruct() {
		return nameStruct;
	}

	public void setNameStruct(String nameStruct) {
		this.nameStruct = nameStruct;
	}

	public int getNumberResult() {
		return numberResult;
	}

	public void setNumberResult(int numberResult) {
		this.numberResult = numberResult;
	}

	public TCComponentBOMViewRevision getResult() {
		return result;
	}

	public void setResult(TCComponentBOMViewRevision result) {
		this.result = result;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	private int numberResult; // 0 - ������, 1 - ������� ������, 2 - ������ ����
	private TCComponentBOMViewRevision result;
	private String error, nameStruct;
}

class UserInformation {
	public String getGroupStr() {
		return groupStr;
	}

	public void setGroupStr(String groupStr) {
		this.groupStr = groupStr;
	}

	public int getNumberResult() {
		return numberResult;
	}

	public void setNumberResult(int numberResult) {
		this.numberResult = numberResult;
	}

	public TCComponentUser getUser() {
		return user;
	}

	public void setUser(TCComponentUser user) {
		this.user = user;
	}

	public TCComponentGroup getGroup() {
		return group;
	}

	public void setGroup(TCComponentGroup group) {
		this.group = group;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	private int numberResult; // 0 - ������, 1 - ������� ������� ������, 2 - ������ ���, ������� ��� ��
								// ��������
	private TCComponentUser user;
	private TCComponentGroup group;
	private String groupStr;
	private String error;
}

class DateForExcelFile {
	public String getStrukt() {
		return strukt;
	}

	public void setStrukt(String strukt) {
		this.strukt = strukt;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getPk() {
		return pk;
	}

	public void setPk(String pk) {
		this.pk = pk;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getOldOwner() {
		return oldOwner;
	}

	public void setOldOwner(String oldOwner) {
		this.oldOwner = oldOwner;
	}

	public String getNewOlder() {
		return newOlder;
	}

	public void setNewOlder(String newOlder) {
		this.newOlder = newOlder;
	}

	private String pk = null, strukt = null, action = null, oldOwner = null, newOlder = null, error = null;
}