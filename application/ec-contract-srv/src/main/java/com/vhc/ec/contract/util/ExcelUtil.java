package com.vhc.ec.contract.util;

import java.text.SimpleDateFormat;

import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelUtil {
	private static final String DATE_COLUMN_FORMAT = "dd/MM/yyyy";
	private static final SimpleDateFormat dff = new SimpleDateFormat(DATE_COLUMN_FORMAT);

	public static String getCellValue(Cell cell) {
		Object cellValue = null;
		if (cell != null) {
			CellType cellType = cell.getCellType();
			switch (cellType) {
			case BOOLEAN:
				cellValue = cell.getBooleanCellValue();
				break;
			case FORMULA:
				Workbook workbook = cell.getSheet().getWorkbook();
				FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
				cellValue = evaluator.evaluate(cell).getNumberValue();
				break;
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					cellValue = dff.format(cell.getDateCellValue());
				} else {
					Double value = cell.getNumericCellValue();

					if (value == Math.ceil(value)) {
						cellValue = (long) Math.ceil(value);
					} else {
						cellValue = value;
					}
				}

				break;
			case STRING:
				cellValue = cell.getStringCellValue();
				break;
			case _NONE:
			case BLANK:
			case ERROR:
				break;
			default:
				break;
			}
		}

		return cellValue != null ? formateCellValue(cellValue) : null;
	}

	public static String columnToLetter(int colIndex) {
		int temp;
		String letter = "";
		while (colIndex > 0) {
			temp = (colIndex - 1) % 26;
			letter = Character.toString(temp + 65) + letter;
			colIndex = (colIndex - temp - 1) / 26;
		}
		return letter;
	}

	private static String formateCellValue(Object cellValue){
		String valueStr = String.valueOf(cellValue);
		valueStr = removeAllControlChars(valueStr);
		return valueStr.trim();
	}

	private static String removeNoneAscii(String str){
		return str.replaceAll("[^\\x00-\\x7F]", "");
	}

	private static String removeNonePrintable(String str){ // All Control Char
		return str.replaceAll("[\\p{C}]", "");
	}

	private static String removeOthersControlChar(String str){ // Some Control Char
		return str.replaceAll("[\\p{Cntrl}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
	}

	private static String removeAllControlChars(String str)
	{
		return removeNonePrintable(str).replaceAll("[\\r\\n\\t]", "");
	}
}
