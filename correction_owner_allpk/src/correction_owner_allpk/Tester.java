package com.kamaz.correction_owner_allpk;

import org.eclipse.core.expressions.PropertyTester;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCSession;

public class Tester extends PropertyTester {
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (property == null || !property.equals("selectedforoprole")) {
			return false;
		}

		TCSession session = (TCSession) AIFUtility.getDefaultSession();

		TCComponentGroup group = session.getCurrentGroup();

		String groupString = group.toString();

		String userName = session.getUserName();

		if (groupString.toLowerCase().equals("dba") || userName.equals("guzenko_ea") || userName.equals("guzenkoea")) {
			return true;
		}

		return false;
	}
}