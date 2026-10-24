package com.cosatcal.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosatcal.model.Material;
import com.cosatcal.model.Parameters;
import com.cosatcal.model.Thickness;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExcelRepositoryTest {

  @TempDir
  Path tempDir;

  private Path file;

  @BeforeEach
  void setUp() {
    file = tempDir.resolve("data.xlsx");
  }

  // ---------- Helpers to build test workbooks ----------

  private static void row(Sheet sheet, int index, Object... values) {
    Row row = sheet.createRow(index);
    for (int i = 0; i < values.length; i++) {
      Object value = values[i];
      if (value == null) {
        continue; // leave the cell empty
      }
      Cell cell = row.createCell(i);
      if (value instanceof Number number) {
        cell.setCellValue(number.doubleValue());
      } else {
        cell.setCellValue(value.toString());
      }
    }
  }

  private static XSSFWorkbook validWorkbook() {
    XSSFWorkbook wb = new XSSFWorkbook();

    Sheet parameters = wb.createSheet("Parametri");
    row(parameters, 0, "chiave", "valore");
    row(parameters, 1, "extra_sfrido", 20);
    row(parameters, 2, "extra_materiale", 30);
    row(parameters, 3, "extra_gas", 30);
    row(parameters, 4, "costo_macchina", 200);
    row(parameters, 5, "costo_pacco", 170);
    row(parameters, 6, "pacco_gas_m3", 168);

    Sheet materials = wb.createSheet("Materiali");
    row(materials, 0, "nome", "prezzo");
    row(materials, 1, "Inox", 3);
    row(materials, 2, "Alluminio", null); // no price

    Sheet inox = wb.createSheet("Inox");
    row(inox, 0, "spessore_mm", "codice", "peso_kgm2", "consumo_gas_m3h");
    row(inox, 1, 1.0, "14301", 7.85, 24);
    row(inox, 2, 2.0, "14301", 15.7, 24);

    Sheet aluminum = wb.createSheet("Alluminio");
    row(aluminum, 0, "spessore_mm", "codice", "peso_kgm2", "consumo_gas_m3h");
    row(aluminum, 1, 6.0, "AW5754", null, 72); // no weight

    return wb;
  }

  private void save(XSSFWorkbook wb) throws IOException {
    try (OutputStream out = Files.newOutputStream(file)) {
      wb.write(out);
    }
    wb.close();
  }

  private ExcelRepository loadedRepository() throws IOException {
    save(validWorkbook());
    ExcelRepository repository = new ExcelRepository(file);
    repository.load();
    return repository;
  }

  // ---------- Parameters ----------

  @Test
  void readsParameters() throws IOException {
    Parameters p = loadedRepository().parameters();

    assertEquals(20.0, p.scrapPct());
    assertEquals(30.0, p.materialMarkupPct());
    assertEquals(30.0, p.gasMarkupPct());
    assertEquals(0, new BigDecimal("200").compareTo(p.machineCostPerHourEur()));
    assertEquals(0, new BigDecimal("170").compareTo(p.gasPackCostEur()));
    assertEquals(168.0, p.gasPackM3());
  }

  @Test
  void acceptsNumbersWrittenAsTextWithComma() throws IOException {
    XSSFWorkbook wb = validWorkbook();
    row(wb.getSheet("Parametri"), 1, "extra_sfrido", "20,5");
    save(wb);

    ExcelRepository repository = new ExcelRepository(file);
    repository.load();

    assertEquals(20.5, repository.parameters().scrapPct());
  }

  @Test
  void missingParameterNamesTheKey() throws IOException {
    XSSFWorkbook wb = validWorkbook();
    wb.getSheet("Parametri").removeRow(wb.getSheet("Parametri").getRow(6)); // pacco_gas_m3
    save(wb);

    DataLoadException e = assertThrows(DataLoadException.class, () -> new ExcelRepository(file).load());

    assertTrue(e.getMessage().contains("pacco_gas_m3"));
  }

  // ---------- Materials ----------

  @Test
  void readsMaterialsInOrderAndEmptyPriceIsNull() throws IOException {
    List<Material> materials = loadedRepository().materials();

    assertEquals(List.of("Inox", "Alluminio"), materials.stream().map(Material::name).toList());
    assertEquals(0, new BigDecimal("3").compareTo(materials.get(0).pricePerKgEur()));
    assertNull(materials.get(1).pricePerKgEur());
  }

  // ---------- Thicknesses ----------

  @Test
  void readsThicknesses() throws IOException {
    List<Thickness> thicknesses = loadedRepository().thicknesses("Inox");

    assertEquals(2, thicknesses.size());
    assertEquals(new Thickness(2.0, "14301", 15.7, 24.0), thicknesses.get(1));
  }

  @Test
  void emptyWeightCellBecomesNaN() throws IOException {
    Thickness t = loadedRepository().thicknesses("Alluminio").get(0);

    assertEquals(6.0, t.thicknessMm());
    assertEquals("AW5754", t.code());
    assertTrue(Double.isNaN(t.weightKgM2()));
    assertEquals(72.0, t.gasConsumptionM3h());
  }

  @Test
  void unknownMaterialGivesEmptyList() throws IOException {
    assertTrue(loadedRepository().thicknesses("Titanio").isEmpty());
  }

  // ---------- Errors ----------

  @Test
  void missingMaterialSheetNamesTheSheet() throws IOException {
    XSSFWorkbook wb = validWorkbook();
    wb.removeSheetAt(wb.getSheetIndex("Inox"));
    save(wb);

    DataLoadException e = assertThrows(DataLoadException.class, () -> new ExcelRepository(file).load());

    assertTrue(e.getMessage().contains("Inox"));
  }

  @Test
  void missingColumnNamesTheColumn() throws IOException {
    XSSFWorkbook wb = validWorkbook();
    wb.removeSheetAt(wb.getSheetIndex("Materiali"));
    Sheet materials = wb.createSheet("Materiali");
    row(materials, 0, "nome"); // "prezzo" column missing
    row(materials, 1, "Inox");
    save(wb);

    DataLoadException e = assertThrows(DataLoadException.class, () -> new ExcelRepository(file).load());

    assertTrue(e.getMessage().contains("prezzo"));
  }

  @Test
  void textWhereNumberExpectedReportsSheetAndRow() throws IOException {
    XSSFWorkbook wb = validWorkbook();
    row(wb.getSheet("Inox"), 2, 2.0, "14301", "abc", 24);
    save(wb);

    DataLoadException e = assertThrows(DataLoadException.class, () -> new ExcelRepository(file).load());

    assertTrue(e.getMessage().contains("Inox"), "Message was: " + e.getMessage());
    assertTrue(e.getMessage().contains("3"), "Message was: " + e.getMessage());
  }

  @Test
  void missingFileThrowsDataLoadException() {
    DataLoadException e = assertThrows(DataLoadException.class,
        () -> new ExcelRepository(tempDir.resolve("nope.xlsx")).load());

    assertTrue(e.getCause() instanceof NoSuchFileException);
  }

  // ---------- State ----------

  @Test
  void usingTheRepositoryBeforeLoadFails() {
    ExcelRepository repository = new ExcelRepository(file);

    assertThrows(IllegalStateException.class, repository::parameters);
    assertThrows(IllegalStateException.class, repository::materials);
  }

  @Test
  void failedReloadKeepsThePreviousData() throws IOException {
    ExcelRepository repository = loadedRepository();

    // Break the file and reload
    XSSFWorkbook broken = validWorkbook();
    broken.removeSheetAt(broken.getSheetIndex("Materiali"));
    save(broken);

    assertThrows(DataLoadException.class, repository::load);

    // The old data is still there
    assertEquals(20.0, repository.parameters().scrapPct());
    assertEquals(2, repository.materials().size());
  }

  @Test
  void reloadPicksUpChangesInTheFile() throws IOException {
    ExcelRepository repository = loadedRepository();

    XSSFWorkbook wb = validWorkbook();
    row(wb.getSheet("Parametri"), 1, "extra_sfrido", 25);
    save(wb);
    repository.load();

    assertEquals(25.0, repository.parameters().scrapPct());
  }
}
