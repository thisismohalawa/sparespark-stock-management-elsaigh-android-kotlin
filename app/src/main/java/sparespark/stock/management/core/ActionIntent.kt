package sparespark.stock.management.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import sparespark.stock.management.R
import sparespark.stock.management.core.view.isPhoneNumberValid
import sparespark.stock.management.core.view.toStringFullNumberFormat
import sparespark.stock.management.data.model.stock.Stock
import java.io.File
import java.io.FileOutputStream

internal fun Context.actionOpenWhatsApp(phoneNum: String): Unit = try {
    if (phoneNum.isPhoneNumberValid()) {
        val globalNum: String = if (phoneNum.startsWith("+20")) phoneNum else "+2$phoneNum"
        val intent = Intent(
            Intent.ACTION_VIEW, Uri.parse(
                String.format(
                    "https://api.whatsapp.com/send?phone=%s&text=%s", globalNum, ""
                )
            )
        )
        startActivity(intent)
    } else toastInvalidNumber(this)
} catch (e: Exception) {
    toastInvalidAction(this)
}

internal fun Context.actionDial(phoneNum: String): Unit = try {
    if (phoneNum.isPhoneNumberValid()) {
        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNum"))
        startActivity(callIntent)
    } else toastInvalidNumber(this)
} catch (e: Exception) {
    toastInvalidAction(this)
}

internal fun Context.actionShareText(content: String) = try {
    if (content.isNotBlank()) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Sparespark Apps")
        intent.putExtra(Intent.EXTRA_TEXT, content)
        this.startActivity(Intent.createChooser(intent, this.getString(R.string.share_action)))
    } else toastInvalidAction(this)
} catch (e: Exception) {
    toastInvalidAction(this)
}

private fun toastInvalidNumber(context: Context) =
    Toast.makeText(context, context.getString(R.string.invalid_phone_num), Toast.LENGTH_SHORT)
        .show()

private fun toastInvalidAction(context: Context) =
    Toast.makeText(context, context.getString(R.string.invalid_action), Toast.LENGTH_SHORT).show()


private fun XSSFWorkbook.getSubHeaderCellStyle(): XSSFCellStyle {
    val headerStyle: XSSFCellStyle = this.createCellStyle()
    headerStyle.setAlignment(HorizontalAlignment.CENTER)
    return headerStyle
}

private fun XSSFWorkbook.getHeaderCellStyle(): XSSFCellStyle {
    val headerStyle = this.createCellStyle()
    val font = this.createFont()
    font.bold = true
    font.color = IndexedColors.WHITE.getIndex()
    headerStyle.setAlignment(HorizontalAlignment.CENTER)
    headerStyle.fillForegroundColor = IndexedColors.BLUE_GREY.getIndex()
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    headerStyle.setFont(font)
    return headerStyle
}

internal suspend fun exportListToExcel(list: List<Stock>) = launchAWithContextScope {
    try {
        val strDate = getCalendarDateTime("dd-MM-yyyy-HH-mm")
        val root = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "ElsaighApp-Backups"
        )
        if (!root.exists()) root.mkdirs()
        val path = File(root, "/$strDate.xlsx")

        val workbook = XSSFWorkbook()
        val outputStream = withContext(Dispatchers.IO) {
            FileOutputStream(path)
        }


        val sheet: XSSFSheet = workbook.createSheet("Data-Backup")

        var row: XSSFRow = sheet.createRow(0)

        var cell: XSSFCell = row.createCell(0)
        cell.setCellValue("Creation Date")
        cell.cellStyle = workbook.getHeaderCellStyle()

        cell = row.createCell(1)
        cell.setCellValue("Client")
        cell.cellStyle = workbook.getHeaderCellStyle()

        cell = row.createCell(2)
        cell.setCellValue("Type")
        cell.cellStyle = workbook.getHeaderCellStyle()

        cell = row.createCell(3)
        cell.setCellValue("City")
        cell.cellStyle = workbook.getHeaderCellStyle()

        cell = row.createCell(4)
        cell.setCellValue("Gram Price")
        cell.cellStyle = workbook.getHeaderCellStyle()

        cell = row.createCell(5)
        cell.setCellValue("Quantity")
        cell.cellStyle = workbook.getHeaderCellStyle()

        cell = row.createCell(6)
        cell.setCellValue("Total")
        cell.cellStyle = workbook.getHeaderCellStyle()

        cell = row.createCell(7)
        cell.setCellValue("is Active")
        cell.cellStyle = workbook.getHeaderCellStyle()

        cell = row.createCell(8)
        cell.setCellValue("Update By")
        cell.cellStyle = workbook.getHeaderCellStyle()

        cell = row.createCell(9)
        cell.setCellValue("Update At")
        cell.cellStyle = workbook.getHeaderCellStyle()

        cell = row.createCell(10)
        cell.setCellValue("Comment")
        cell.cellStyle = workbook.getHeaderCellStyle()

        for (i in list.indices) {
            row = sheet.createRow(i + 1)

            cell = row.createCell(0)
            if (list[i].creationDateCustom.isNotBlank()) cell.setCellValue(list[i].creationDateCustom)
            else cell.setCellValue(list[i].creationDate)
            cell.cellStyle = workbook.getSubHeaderCellStyle()
            sheet.setColumnWidth(0, (list[i].creationDateCustom.length + 30) * 256)

            cell = row.createCell(1)
            cell.setCellValue(list[i].client)
            cell.cellStyle = workbook.getSubHeaderCellStyle()
            sheet.setColumnWidth(1, (list[i].creationDate.length + 10) * 256)

            cell = row.createCell(2)
            cell.setCellValue(list[i].operationType.getOperationTypeStringValue())
            cell.cellStyle = workbook.getSubHeaderCellStyle()
            sheet.setColumnWidth(2, (list[i].creationDate.length + 10) * 256)

            cell = row.createCell(3)
            cell.setCellValue(list[i].city)
            cell.cellStyle = workbook.getSubHeaderCellStyle()
            sheet.setColumnWidth(3, (list[i].creationDate.length + 10) * 256)

            cell = row.createCell(4)
            cell.setCellValue(list[i].assetGramPrice)
            cell.cellStyle = workbook.getSubHeaderCellStyle()
            sheet.setColumnWidth(4, (list[i].creationDate.length + 10) * 256)

            cell = row.createCell(5)
            cell.setCellValue(list[i].assetQuantity)
            cell.cellStyle = workbook.getSubHeaderCellStyle()
            sheet.setColumnWidth(5, (list[i].creationDate.length + 10) * 256)

            cell = row.createCell(6)
            cell.setCellValue((list[i].assetGramPrice * list[i].assetQuantity).toStringFullNumberFormat())
            cell.cellStyle = workbook.getSubHeaderCellStyle()
            sheet.setColumnWidth(6, (list[i].creationDate.length + 10) * 256)

            cell = row.createCell(7)
            cell.setCellValue((list[i].active.getActiveTypeStringValue()))
            cell.cellStyle = workbook.getSubHeaderCellStyle()
            sheet.setColumnWidth(7, (list[i].creationDate.length + 10) * 256)

            cell = row.createCell(8)
            cell.setCellValue((list[i].lastUpdateBy))
            cell.cellStyle = workbook.getSubHeaderCellStyle()
            sheet.setColumnWidth(8, (list[i].creationDate.length + 10) * 256)

            cell = row.createCell(9)
            cell.setCellValue((list[i].lastUpdateDate))
            cell.cellStyle = workbook.getSubHeaderCellStyle()
            sheet.setColumnWidth(9, (list[i].creationDate.length + 30) * 256)

            cell = row.createCell(10)
            cell.setCellValue((list[i].details))
            cell.cellStyle = workbook.getSubHeaderCellStyle()
            sheet.setColumnWidth(10, (list[i].creationDate.length + 10) * 256)

        }
        workbook.write(outputStream)
        withContext(Dispatchers.IO) {
            outputStream.close()
        }

    } catch (e: Exception) {
        Log.d(TAG, "exportListToExcel: ${e.message}")
        e.printStackTrace()
    }
}
