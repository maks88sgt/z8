package org.zenframework.z8.server.reports;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.IDesignEngineFactory;

import org.zenframework.z8.server.base.model.actions.ReadAction;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.exceptions.UnsupportedParameterException;

public class ReportOptions {
    private String format = ReportConstants.FORMAT_PDF;

    public int indentGroupsBy = ReportConstants.DEFAULT_GROUP_INDENTATION;

    public boolean markGroupLevel = true;

    public boolean markTotals = true;

    public boolean markGrandTotals = true;

    public boolean dropGroupDetail = false;

    public boolean useBlackWhiteColors = true;

    public int pagesWide = 1;

    public boolean scaleContent = true;

    public boolean splitContent = false;

    public int m_pageOverlapping = ReportConstants.DEFAULT_PAGE_OVERLAPPING;

    public String reportFolder = ReportConstants.DEFAULT_DYN_REPORT_FOLDER;
    public String reportTemplate = ReportConstants.DEFAULT_REPORT_DESIGN;

    public Collection<ReadAction> actions = null;

    public Map<String, String> headers = new HashMap<String, String>();

    private float pageHeight = 0;
    private float pageWidth = 0;
    private float leftMargin = 0;
    private float rightMargin = 0;
    private float topMargin = 0;
    private float bottomMargin = 0;

    private static IReportEngine reportEngine = null;
    private static IDesignEngine designEngine = null;

    static private File getBaseFolder() {
        return ApplicationServer.workingPath();
    }

    static public File getReportOutputFolder() {
        File folder = new File(getBaseFolder(), ReportConstants.ReportOutputFolder);
        folder.mkdirs();
        return folder;
    }

    public ReportOptions() {
        initializeEngine();
    }

    private void initializeEngine() throws RuntimeException {
        if(reportEngine != null) {
            return;
        }

        try {
            EngineConfig engineConfig = new EngineConfig();
            DesignConfig designConfig = new DesignConfig();

            Platform.startup(engineConfig);

            IReportEngineFactory reportEngineFactory = (IReportEngineFactory)Platform
                    .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
            reportEngine = reportEngineFactory.createReportEngine(engineConfig);

            IDesignEngineFactory designEngineFactory = (IDesignEngineFactory)Platform
                    .createFactoryObject(IDesignEngineFactory.EXTENSION_DESIGN_ENGINE_FACTORY);
            designEngine = designEngineFactory.createDesignEngine(designConfig);
        }
        catch(BirtException e) {
            throw new RuntimeException(e);
        }
    }

    public IReportEngine reportEngine() {
        return reportEngine;
    }

    public IDesignEngine designEngine() {
        return designEngine;
    }

    public String getFormat() {
        return format;
    }

    public void setTemplate(String template) {
        if(template != null) {
            this.reportTemplate = template;
        }
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setFormat(ReportType type) {
        switch(type) {
        case PDF:
            format = ReportConstants.FORMAT_PDF;
            break;
        case RTF:
            format = ReportConstants.FORMAT_WORD;
            break;
        case XLS:
            format = ReportConstants.FORMAT_EXCEL;
            break;
        default:
            throw new UnsupportedParameterException();
        }
    }

    private String getReportDesignFileName(String format) {
        return format + '_' + reportTemplate;
    }

    public File getReportDesign() {
        String fileName = getReportDesignFileName(getFormat());

        File file = new File(new File(ApplicationServer.workingPath(), reportFolder), fileName);

        if(file.exists()) {
            return file;
        }

        return new File(new File(ApplicationServer.workingPath(), reportFolder), reportTemplate);
    }

    public float getPageWidth() {
        return pageWidth;
    }

    public void setPageWidth(float pageWidth) {
        this.pageWidth = pageWidth;
    }

    public float getPageHeight() {
        return pageHeight;
    }

    public void setPageHeight(float pageHeight) {
        this.pageHeight = pageHeight;
    }

    public float getLeftMargin() {
        return leftMargin;
    }

    public void setLeftMargin(float leftMargin) {
        this.leftMargin = leftMargin;
    }

    public float getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(float rightMargin) {
        this.rightMargin = rightMargin;
    }

    public float getTopMargin() {
        return topMargin;
    }

    public void setTopMargin(float topMargin) {
        this.topMargin = topMargin;
    }

    public float getBottomMargin() {
        return bottomMargin;
    }

    public void setBottomMargin(float bottomMargin) {
        this.bottomMargin = bottomMargin;
    }

    public float getHorizontalMargins() {
        return leftMargin + rightMargin;
    }

    public float getVerticalMargins() {
        return topMargin + bottomMargin;
    }

    public String documentName() {
        String name = headers.get(ReportConstants.FIRSTPAGE_CAPTIONCENTER);

        return name != null ? name : "report";
    }

}