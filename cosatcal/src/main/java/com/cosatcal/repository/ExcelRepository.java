package com.cosatcal.repository;

import com.cosatcal.model.Material;
import com.cosatcal.model.Parameters;
import com.cosatcal.model.Thickness;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelRepository {

  // Data briught from the Execl file
  private static final String SHEET_PARAMETERS = "Parametri";
  private static final String SHEET_MATERIALS = "Materiali";

  private static final String COL_KEY = "chiave";
  private static final String COL_VALUE = "valore";
  private static final String P_SCRAP = "extra_sfrido";
  private static final String P_MATERIAL_MARKUP = "extra_materiale";
  private static final String P_GAS_MARKUP = "extra_gas";
  private static final String P_MACHINE_COST = "costo_macchina";
  private static final String P_PACK_COST = "costo_pacco";
  private static final String P_PACK_M3 = "pacco_gas_m3";

  private static final String COL_NAME = "nome";
  private static final String COL_PRICE = "prezzo";

  private static final String COL_THICKNESS = "spessore_mm";
  private static final String COL_CODE = "codice";
  private static final String COL_WEIGHT = "peso_kgm2";
  private static final String COL_GAS = "consumo_gas_m3h";

  private record Snapshot(
      Parameters parameters,
      List<Material> materials,
      Map<String, List<Thickness>> thicknesses) {
  }

  private final Path file;
  private volatile Snapshot snapshot;

  public ExcelRepository(Path file) {
    this.file = file;
  }

  public void load() {
    try (InputStream in = Files.newInputStream(file);
        Workbook workbook = WorkbookFactory.create(in)) {

      FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

      Parameters parameters = readParameters(table(workbook, SHEET_PARAMETERS, evaluator));
      List<Material> materials = readMaterials(table(workbook, SHEET_MATERIALS, evaluator));

      Map<String, List<Thickness>> thicknesses = new LinkedHashMap<>();
      for (Material material : materials) {
        thicknesses.put(
            material.name(), readThicknesses(table(workbook, material.name(), evaluator)));
      }

      snapshot = new Snapshot(parameters, List.copyOf(materials), thicknesses);

    } catch (IOException e) {
      throw new DataLoadException("Impossibile leggere il file " + file + ": " + e.getMessage(), e);
    }
  }

  public Parameters parameters() {
    return current().parameters();
  }

  public List<Material> materials() {
    return current().materials();
  }

  public List<Thickness> thicknesses(String materialName) {
    return current().thicknesses().getOrDefault(materialName, List.of());
  }

  private Snapshot current() {
    Snapshot s = snapshot;
    if (s == null) {
      throw new IllegalStateException("I dati non sono stati caricati: chiama load() prima.");
    }
    return s;
  }

  private Parameters readParameters(Table table) {
    Map<String, BigDecimal> values = new HashMap<>();
    for (Row row : table.rows(COL_KEY)) {
      values.put(table.text(row, COL_KEY), table.decimal(row, COL_VALUE));
    }

    return new Parameters(
        required(values, P_SCRAP).doubleValue(),
        required(values, P_MATERIAL_MARKUP).doubleValue(),
        required(values, P_GAS_MARKUP).doubleValue(),
        required(values, P_MACHINE_COST),
        required(values, P_PACK_COST),
        required(values, P_PACK_M3).doubleValue());
  }

  private BigDecimal required(Map<String, BigDecimal> values, String key) {
    BigDecimal value = values.get(key);
    if (value == null) {
      throw new DataLoadException(
          "Nel foglio " + SHEET_PARAMETERS + " manca il valore di '" + key + "'.");
    }
    return value;
  }

  private List<Material> readMaterials(Table table) {
    List<Material> result = new ArrayList<>();
    for (Row row : table.rows(COL_NAME)) {
      result.add(new Material(table.text(row, COL_NAME), table.decimal(row, COL_PRICE)));
    }
    return result;
  }

  private List<Thickness> readThicknesses(Table table) {
    List<Thickness> result = new ArrayList<>();
    for (Row row : table.rows(COL_THICKNESS)) {
      Double thickness = table.number(row, COL_THICKNESS);
      String code = table.text(row, COL_CODE);
      Double weight = table.number(row, COL_WEIGHT);
      Double gas = table.number(row, COL_GAS);

      result.add(new Thickness(
          thickness,
          code == null ? "" : code,
          weight == null ? Double.NaN : weight,
          gas == null ? Double.NaN : gas));
    }
    return Collections.unmodifiableList(result);
  }

  private static Table table(Workbook workbook, String sheetName, FormulaEvaluator evaluator) {
    Sheet sheet = workbook.getSheet(sheetName);
    if (sheet == null) {
      throw new DataLoadException(
          "Foglio '" + sheetName + "' non trovato. Il nome deve essere identico "
              + "(maiuscole e spazi) a quello in " + SHEET_MATERIALS + ".");
    }
    return new Table(sheet, evaluator);
  }

  private static final class Table {
    private final Sheet sheet;
    private final FormulaEvaluator evaluator;
    private final Map<String, Integer> columns = new HashMap<>();

    Table(Sheet sheet, FormulaEvaluator evaluator) {
      this.sheet = sheet;
      this.evaluator = evaluator;

      Row header = sheet.getRow(sheet.getFirstRowNum());
      if (header != null) {
        for (Cell cell : header) {
          Object value = value(cell);
          if (value instanceof String name) {
            columns.put(name, cell.getColumnIndex());
          }
        }
      }
    }

    List<Row> rows(String keyColumn) {
      List<Row> result = new ArrayList<>();
      for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row != null && raw(row, keyColumn) != null) {
          result.add(row);
        }
      }
      return result;
    }

    String text(Row row, String column) {
      Object value = raw(row, column);
      if (value == null) {
        return null;
      }
      if (value instanceof Double number) {
        return BigDecimal.valueOf(number).stripTrailingZeros().toPlainString();
      }
      return value.toString();
    }

    Double number(Row row, String column) {
      Object value = raw(row, column);
      if (value == null) {
        return null;
      }
      if (value instanceof Double number) {
        return number;
      }
      try {
        return Double.parseDouble(value.toString().replace(',', '.'));
      } catch (NumberFormatException e) {
        throw invalid(row, column, value);
      }
    }

    BigDecimal decimal(Row row, String column) {
      Object value = raw(row, column);
      if (value == null) {
        return null;
      }
      if (value instanceof Double number) {
        return BigDecimal.valueOf(number);
      }
      try {
        return new BigDecimal(value.toString().replace(',', '.'));
      } catch (NumberFormatException e) {
        throw invalid(row, column, value);
      }
    }

    private Object raw(Row row, String column) {
      Integer index = columns.get(column);
      if (index == null) {
        throw new DataLoadException(
            "Nel foglio '" + sheet.getSheetName() + "' manca la colonna '" + column + "'.");
      }
      return value(row.getCell(index));
    }

    private DataLoadException invalid(Row row, String column, Object value) {
      return new DataLoadException(
          "Foglio '" + sheet.getSheetName() + "', riga " + (row.getRowNum() + 1)
              + ", colonna '" + column + "': '" + value + "' non è un numero.");
    }

    private Object value(Cell cell) {
      if (cell == null) {
        return null;
      }
      CellType type = cell.getCellType();
      if (type == CellType.FORMULA) {
        type = evaluator.evaluateFormulaCell(cell);
      }
      switch (type) {
        case NUMERIC:
          return cell.getNumericCellValue();
        case STRING:
          String text = cell.getStringCellValue().trim();
          return text.isEmpty() ? null : text;
        default:
          return null;
      }
    }
  }
}
