package org.happermission

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.awt.BorderLayout
import java.io.File
import java.io.FileInputStream
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

fun createResultPanel(project: Project, toolWindow: ToolWindow): JPanel {
    val panel = JPanel(BorderLayout())

    val model = DefaultTableModel()
    val table = JBTable(model)
    table.autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
    toolWindow.component.putClientProperty("model", model)
    toolWindow.component.putClientProperty("table", table)

    loadExcelFile(File((project.basePath ?: "") + "/haper_report.xlsx"), model, table)

    val scrollPane = JBScrollPane(table)

    panel.add(scrollPane)
    return panel
}


@OptIn(ExperimentalStdlibApi::class)
fun loadExcelFile(file: File, model: DefaultTableModel, table: JBTable) {
    if (!file.exists()) return
    FileInputStream(file).use { inputStream ->
        val workbook: Workbook = XSSFWorkbook(inputStream)
        val sheet = workbook.getSheetAt(0)

        model.rowCount = 0
        model.columnCount = 0

        val headerRow = sheet.getRow(sheet.firstRowNum)
        val headers = Array(headerRow.lastCellNum.toInt()) { "" }
        for (i in 0 until headerRow.lastCellNum) {
            headers[i] = getCellValueAsString(headerRow.getCell(i))
        }
        model.setColumnIdentifiers(headers)

        for (i in sheet.firstRowNum + 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val rowData = Array(row.lastCellNum.toInt()) { "" }
            for (j in 0 until row.lastCellNum) {
                rowData[j] = getCellValueAsString(row.getCell(j))
            }
            model.addRow(rowData)
        }
        workbook.close()
    }

//    val columnCount = table.columnCount
//    val columnModel = table.columnModel
//    for (i in 0..<columnCount) {
//        columnModel.getColumn(i).preferredWidth = 300
//    }
}

fun getCellValueAsString(cell: Cell?): String {
    if (cell == null) return ""

    return when (cell.cellType) {
        CellType.STRING -> cell.stringCellValue
        CellType.NUMERIC -> cell.numericCellValue.toInt().toString()
        CellType.BOOLEAN -> cell.booleanCellValue.toString()
        CellType.FORMULA -> cell.cellFormula
        else -> ""
    }
}