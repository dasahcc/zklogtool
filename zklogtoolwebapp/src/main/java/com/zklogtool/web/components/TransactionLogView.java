/* 
 * Copyright 2014 Alen Caljkusic.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zklogtool.web.components;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.ZooDefs.OpCode;
import org.apache.zookeeper.txn.CheckVersionTxn;
import org.apache.zookeeper.txn.CreateTxn;
import org.apache.zookeeper.txn.DeleteTxn;
import org.apache.zookeeper.txn.SetACLTxn;
import org.apache.zookeeper.txn.SetDataTxn;
import org.tepi.filtertable.FilterTable;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.zklogtool.data.DataDirTransactionLogFileList;
import com.zklogtool.data.DataState;
import com.zklogtool.data.Transaction;
import com.zklogtool.data.TransactionIterator;
import com.zklogtool.data.TransactionLog;
import com.zklogtool.data.TransactionState;
import com.zklogtool.printer.TransactionPrinter;
import com.zklogtool.printer.UnicodeDecoder;
import com.zklogtool.reader.SnapshotFileReader;
import com.zklogtool.reader.TransactionLogReaderFactory;
import com.zklogtool.util.DataDirHelper;
import com.zklogtool.util.Util;

public class TransactionLogView extends CustomComponent {

    /*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */
    @AutoGenerated
    private AbsoluteLayout mainLayout;
    @AutoGenerated
    private HorizontalSplitPanel horizontalSplitPanel_2;
    @AutoGenerated
    private VerticalLayout verticalLayout_1;
    @AutoGenerated
    private Label descriptionLabel;
    @AutoGenerated
    private Button reconstructDataTreeButton;
    @AutoGenerated
    private FilterTable filterTable;

    public TransactionLogView(final File transactionLogFile, final File snapshotDir, final boolean follow, final boolean startFromLast, final TabSheet displayTabSheet, final String name) {
        buildMainLayout();
        setCompositionRoot(mainLayout);

        descriptionLabel.setContentMode(ContentMode.PREFORMATTED);

        final Container container = new IndexedContainer();
        container.addContainerProperty("zxid", String.class, 0);
        container.addContainerProperty("cxid", String.class, 0);
        container.addContainerProperty("client id", String.class, 0);
        //container.addContainerProperty("time", Date.class, 0);
        container.addContainerProperty("operation", ZkOperations.class, "");
        container.addContainerProperty("path", String.class, "");

        reconstructDataTreeButton.setVisible(false);
        filterTable.setContainerDataSource(container);
        filterTable.setFilterBarVisible(true);
        filterTable.setFilterDecorator(new TransactionFilterDecoder());
        filterTable.setSelectable(true);
        filterTable.setImmediate(true);

        final TransactionLog transactionLog;
        final Iterator<Transaction> iterator;
        final Map<String, Transaction> transactionMap = new HashMap<String, Transaction>();

        filterTable.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                
                if(filterTable.getValue()==null)
                    return;

                StringBuilder description = new StringBuilder();

                TransactionPrinter printer = new TransactionPrinter(description, new UnicodeDecoder());
                
                printer.print(transactionMap.get("0x" + filterTable.getValue().toString()));

                descriptionLabel.setValue(description.toString());

                if (snapshotDir != null && transactionLogFile.isDirectory()) {

                    reconstructDataTreeButton.setVisible(true);

                }

            }
        });

        if (transactionLogFile.isFile()) {

            transactionLog = new TransactionLog(transactionLogFile,
                    new TransactionLogReaderFactory());
        } else {

            transactionLog = new TransactionLog(new DataDirTransactionLogFileList(transactionLogFile),
                    new TransactionLogReaderFactory());
        }

        iterator = transactionLog.iterator();

        if (startFromLast) {

            while (iterator.hasNext()) {

                iterator.next();
            }
        }

        final Runnable fillData = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                while (iterator.hasNext()) {

                    Transaction t = iterator.next();

                    transactionMap.put(Util.longToHexString(t.getTxnHeader().getZxid()), t);

                    Item item = container.addItem(Long.toHexString(t.getTxnHeader().getZxid()));
                    item.getItemProperty("zxid").setValue(
                            Util.longToHexString(t.getTxnHeader().getZxid()));

                    item.getItemProperty("cxid").setValue(
                            Util.longToHexString(t.getTxnHeader().getCxid()));

                    item.getItemProperty("client id").setValue(
                            Util.longToHexString(t.getTxnHeader().getClientId()));

                    /*item.getItemProperty("time").setValue(
                     new Date(t.getTxnHeader().getTime()));*/
                    switch (t.getTxnHeader().getType()) {

                        case OpCode.create:
                            CreateTxn createTxn = (CreateTxn) t.getTxnRecord();
                            item.getItemProperty("operation").setValue(ZkOperations.CREATE);
                            item.getItemProperty("path").setValue(createTxn.getPath());
                            break;

                        case OpCode.delete:
                            DeleteTxn deleteTxn = (DeleteTxn) t.getTxnRecord();
                            item.getItemProperty("operation").setValue(ZkOperations.DELTE);
                            item.getItemProperty("path").setValue(deleteTxn.getPath());
                            break;

                        case OpCode.setData:
                            SetDataTxn setDataTxn = (SetDataTxn) t.getTxnRecord();
                            item.getItemProperty("operation").setValue(ZkOperations.SET_DATA);
                            item.getItemProperty("path").setValue(setDataTxn.getPath());
                            break;

                        case OpCode.setACL:
                            SetACLTxn setACLTxn = (SetACLTxn) t.getTxnRecord();
                            item.getItemProperty("operation").setValue(ZkOperations.SET_ACL);
                            item.getItemProperty("path").setValue(setACLTxn.getPath());
                            break;

                        case OpCode.check:
                            CheckVersionTxn checkVersionTxn = (CheckVersionTxn) t.getTxnRecord();
                            item.getItemProperty("operation").setValue(ZkOperations.CHECK);
                            item.getItemProperty("path").setValue(checkVersionTxn.getPath());
                            break;

                        case OpCode.multi:
                            item.getItemProperty("operation").setValue(ZkOperations.MULTI);
                            break;

                        case OpCode.createSession:
                            item.getItemProperty("operation").setValue(ZkOperations.CREATE_SESSION);
                            break;

                        case OpCode.closeSession:
                            item.getItemProperty("operation").setValue(ZkOperations.CLOSE_SESSION);
                            break;

                        case OpCode.error:
                            item.getItemProperty("operation").setValue(ZkOperations.ERROR);
                            break;

                    }

                }
            }

        };

        fillData.run();

        Thread monitorThread = new Thread(new Runnable() {

            @Override
            public void run() {

                while (true) {
                    //push UI
                    UI.getCurrent().access(fillData);

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        });

        if (follow) {
            monitorThread.start();

        }

        reconstructDataTreeButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {

                DataDirHelper dataDirHelper = new DataDirHelper(transactionLogFile, snapshotDir);
                List<File> snapshots = dataDirHelper.getSortedSnapshotList();
                DataDirTransactionLogFileList l = new DataDirTransactionLogFileList(transactionLogFile);
                TransactionLog transactionLog = new TransactionLog(l, new TransactionLogReaderFactory());

                File snapFile = null;
                DataState dataState = null;

                long currentZxid = Long.parseLong(filterTable.getValue().toString(), 16);

                int i = snapshots.size() - 1;
                while (i >= 0) {

                    long snapZxid = Util.getZxidFromName(snapshots.get(i).getName());

                    if (snapZxid <= currentZxid) {

                        if (i == 0) {
                            snapFile = snapshots.get(0);
                        } else {
                            snapFile = snapshots.get(i - 1);
                        }

                        break;

                    }

                    i--;

                }

                if(snapFile==null){
                    dispalyNotEnoughDataErrorMessage();
                    return;
                }
                
                long TS = Util.getZxidFromName(snapFile.getName());

                //catch this exception and print error
                SnapshotFileReader snapReader = new SnapshotFileReader(snapFile, TS);

                try {
                    dataState = snapReader.restoreDataState(transactionLog.iterator());
                } catch (Exception ex) {
                    //dispay error dialog
                    //not enough information
                    dispalyNotEnoughDataErrorMessage();
                    return;
                }

                //set iterator to last zxid
                TransactionIterator iterator = transactionLog.iterator();
                Transaction t;

                do {

                    t = iterator.next();

                } while (t.getTxnHeader().getZxid() < TS);

                while (iterator.nextTransactionState() == TransactionState.OK && dataState.getLastZxid() < currentZxid) {
                    dataState.processTransaction(iterator.next());
                }

                HorizontalLayout horizontalLayout = new HorizontalLayout();
                horizontalLayout.setCaption(name + " at zxid 0x" + Long.toString(currentZxid, 16));
                horizontalLayout.addComponent(new SnapshotView(dataState));
                horizontalLayout.setWidth("100%");
                horizontalLayout.setHeight("100%");
                Tab snapshotTab = displayTabSheet.addTab(horizontalLayout);
                snapshotTab.setClosable(true);
                displayTabSheet.setSelectedTab(snapshotTab);

            }

            void dispalyNotEnoughDataErrorMessage() {

                final Window window = new Window("Error");
                window.setModal(true);

                final VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.setMargin(true);
                verticalLayout.addComponent(new Label("Not enough data to reconstruct data tree"));

                window.setContent(verticalLayout);
                UI.getCurrent().addWindow(window);

            }

        });

    }

    @AutoGenerated
    private AbsoluteLayout buildMainLayout() {
        // common part: create layout
        mainLayout = new AbsoluteLayout();
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("100%");

        // top-level component properties
        setWidth("100.0%");
        setHeight("100.0%");

        // horizontalSplitPanel_2
        horizontalSplitPanel_2 = buildHorizontalSplitPanel_2();
        mainLayout.addComponent(horizontalSplitPanel_2,
                "top:0.0px;right:1.0px;bottom:3.0px;left:0.0px;");

        return mainLayout;
    }

    @AutoGenerated
    private HorizontalSplitPanel buildHorizontalSplitPanel_2() {
        // common part: create layout
        horizontalSplitPanel_2 = new HorizontalSplitPanel();
        horizontalSplitPanel_2.setImmediate(false);
        horizontalSplitPanel_2.setWidth("100.0%");
        horizontalSplitPanel_2.setHeight("100.0%");

        // filterTable
        filterTable = new FilterTable();
        filterTable.setImmediate(false);
        filterTable.setWidth("100.0%");
        filterTable.setHeight("100.0%");
        horizontalSplitPanel_2.addComponent(filterTable);

        // verticalLayout_1
        verticalLayout_1 = buildVerticalLayout_1();
        horizontalSplitPanel_2.addComponent(verticalLayout_1);

        return horizontalSplitPanel_2;
    }

    @AutoGenerated
    private VerticalLayout buildVerticalLayout_1() {
        // common part: create layout
        verticalLayout_1 = new VerticalLayout();
        verticalLayout_1.setImmediate(false);
        verticalLayout_1.setWidth("100.0%");
        verticalLayout_1.setHeight("100.0%");
        verticalLayout_1.setMargin(true);
        verticalLayout_1.setSpacing(true);

        // reconstructDataTreeButton
        reconstructDataTreeButton = new Button();
        reconstructDataTreeButton.setCaption("Reconstruct Data Tree");
        reconstructDataTreeButton.setImmediate(true);
        reconstructDataTreeButton.setWidth("-1px");
        reconstructDataTreeButton.setHeight("-1px");
        verticalLayout_1.addComponent(reconstructDataTreeButton);
        verticalLayout_1.setComponentAlignment(reconstructDataTreeButton,
                new Alignment(6));

        // descriptionLabel
        descriptionLabel = new Label();
        descriptionLabel.setImmediate(false);
        descriptionLabel.setWidth("-1px");
        descriptionLabel.setHeight("-1px");
        descriptionLabel.setValue("Select Transaction");
        verticalLayout_1.addComponent(descriptionLabel);
        verticalLayout_1.setExpandRatio(descriptionLabel, 1.0f);

        return verticalLayout_1;
    }

}
