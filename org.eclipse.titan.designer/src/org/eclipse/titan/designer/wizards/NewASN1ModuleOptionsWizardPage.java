/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kristof Szabados
 * */
public final class NewASN1ModuleOptionsWizardPage extends WizardPage {

	private static final String TITLE = "ASN1 Module creation options";
	private static final String DESCRIPTION = "Create the new ASN1 module according to these options";
	private static final String GEN_EXCLUDED = "Generate as excluded from build";
	private static final String GEN_SKELETON = "Generate with module skeleton inserted";

	private Composite pageComposite;
	private Button excludeFromBuildButton;
	private boolean isExcludedFromBuildSelected = false;
	private Button generateSkeletonButton;
	private boolean isGenerateSkeletonSelected = true;

	public NewASN1ModuleOptionsWizardPage() {
		super(TITLE);
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt
	 * .widgets.Composite)
	 */
	@Override
	public void createControl(final Composite parent) {
		pageComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		pageComposite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		pageComposite.setLayoutData(data);

		excludeFromBuildButton = new Button(pageComposite, SWT.CHECK);
		excludeFromBuildButton.setText(GEN_EXCLUDED);
		excludeFromBuildButton.setEnabled(true);
		excludeFromBuildButton.setSelection(false);
		isExcludedFromBuildSelected = false;
		excludeFromBuildButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				isExcludedFromBuildSelected = excludeFromBuildButton.getSelection();
			}
		});

		generateSkeletonButton = new Button(pageComposite, SWT.CHECK);
		generateSkeletonButton.setText(GEN_SKELETON);
		generateSkeletonButton.setEnabled(true);
		generateSkeletonButton.setSelection(true);
		isGenerateSkeletonSelected = true;
		generateSkeletonButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				isGenerateSkeletonSelected = generateSkeletonButton.getSelection();
			}
		});

		setControl(pageComposite);
	}

	public boolean isExcludeFromBuildSelected() {
		return isExcludedFromBuildSelected;
	}

	public boolean isGenerateSkeletonSelected() {
		return isGenerateSkeletonSelected;
	}

	@Override
	public void setWizard(final IWizard newWizard) {
		super.setWizard(newWizard);
	}
}
