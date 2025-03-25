package com.kamaz.correction_owner_allpk;

import java.awt.Frame;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

public class Handler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AIFDesktop activeDesktop = AIFUtility.getActiveDesktop();

		if (activeDesktop != null) {
			try {
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						Part part = new Part();
						part.start();
					}
				};

				Thread thread = new Thread(runnable);
				thread.setDaemon(false);
				thread.start();
			} catch (Exception e) {
				MessageBox messageBox = new MessageBox(new Frame(), e);
				messageBox.setModal(true);
				messageBox.setVisible(true);
			}
		}

		return null;
	}
}
