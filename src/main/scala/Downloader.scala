import sttp.client3._
import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.CollectionConverters._
import org.knowm.xchart.{CategoryChart, CategoryChartBuilder, BitmapEncoder}
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import java.io.{FileInputStream, FileOutputStream}
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.util.Units
import org.apache.poi.ss.usermodel.Workbook
import com.mongodb.client.{MongoClients, MongoCollection, MongoDatabase}
import org.bson.Document
import scala.io.StdIn
import upickle.default.{read => uread, write => uwrite, ReadWriter, macroRW} // Importamos macroRW para generar automáticamente el ReadWriter
import os.{read => _, _} // Excluimos el método read de os-lib
import scala.collection.mutable.ArrayBuffer
import play.api.libs.json._
import org.knowm.xchart.{CategoryChart, CategoryChartBuilder, SwingWrapper}
import org.knowm.xchart.VectorGraphicsEncoder
import org.knowm.xchart.VectorGraphicsEncoder.VectorGraphicsFormat

object Downloader {
    private val client = MongoClients.create("mongodb://localhost:27017")
    private val database: MongoDatabase = client.getDatabase("Horarios")
    private val collection: MongoCollection[Document] = database.getCollection("horario")

    case class Trabajadores(trabajador: String, hmin: Option[Int], hmax: Option[Int], dia: Int, tramo: Int, ntienda: Int, nalmacen: Int)

    def main(args: Array[String]): Unit = {
        val url = "https://knuth.uca.es/moodle/pluginfile.php/12922/mod_folder/content/0/resuelto1.csv?forcedownload=1"
        val fileName = "resuelto1.csv"
        val lines = Source.fromFile(fileName).getLines().drop(1).toList
        val trabajadores = lines.map(_.split(",")(0))
        val horasEnTienda = lines.map(_.split(",")(5).toInt)
        implicit val backend = HttpURLConnectionBackend()
        val request = basicRequest.get(uri"$url")
        val response = request.send()
        response.body match
        case Right(data) =>
            Files.write(Paths.get(fileName), data.getBytes)
            println(s"Archivo descargado correctamente como $fileName")
        case Left(error) =>
            println(s"Error al descargar el archivo: $error")

        var salir = false
        while (!salir) {
            mostrarMenu()
            StdIn.readLine() match {
                case "1" => crearGrafica()
                case "2" => escribirEnMongo()
                case "3" => crearGraficaDesdeMongo()
                case "4" => salir = true
                case _ => println("Introduce una opción válida del 1 al 4")
            }
        }
        client.close()
    }

    private def mostrarMenu(): Unit = {
        println("\n --- MENU ---")
        println("1. Crear gráfica en .docx desde .csv")
        println("2. Escribir los datos en MongoDB")
        println("3. Crear una gráfica con los 3 primeros datos que encuentren en MongoDB")
        println("4. Salir")
    }

    private def crearGrafica(): Unit = {
        val url = "https://knuth.uca.es/moodle/pluginfile.php/12922/mod_folder/content/0/resuelto1.csv?forcedownload=1"
        val fileName = "resuelto1.csv"
        val lines = Source.fromFile(fileName).getLines().drop(1).toList
        val trabajadores = lines.map(_.split(",")(0))
        val horasEnTienda = lines.map(_.split(",")(5).toInt)

        val chart: CategoryChart = new CategoryChartBuilder()
            .width(1000).height(600)
            .title("Horarios en tienda")
            .xAxisTitle("Trabajador")
            .yAxisTitle("Horas en tienda")
            .build()

        chart.addSeries("Horas", trabajadores.asJava, horasEnTienda.map(_.asInstanceOf[Number]).asJava)

        val imgFile = "grafica_horasEnTienda.png"
        BitmapEncoder.saveBitmap(chart, "grafica_horasEnTienda", BitmapFormat.PNG)

        val doc = new XWPFDocument()
        val out = new FileOutputStream("grafica_horasEnTienda.docx")
        val paragraph = doc.createParagraph()
        val run = paragraph.createRun()

        val is = new FileInputStream(imgFile)
        run.addPicture(is, Workbook.PICTURE_TYPE_PNG, imgFile, Units.toEMU(500), Units.toEMU(300))
        is.close()

        doc.write(out)
        out.close()
        doc.close()

        println("¡Documento DOCX creado con la gráfica! Puedes abrirlo con WordPad o Word.")
    }

    private def escribirEnMongo(): Unit = {
        val fileName = "resuelto1.csv"
        val trabajadores: List[Trabajadores] = Source.fromFile(fileName).getLines().drop(1).toList.map { campos =>
            val partes = campos.split(",")
            val hmin: Option[Int] = if (partes(1) == "") None else Some(partes(1).toInt)
            val hmax: Option[Int] = if (partes(2) == "") None else Some(partes(2).toInt)
            Trabajadores(
                trabajador = partes(0),
                hmin,
                hmax,
                dia = partes(3).toInt,
                tramo = partes(4).toInt,
                ntienda = partes(5).toInt,
                nalmacen = partes(6).toInt
            )
        }
        trabajadores.foreach { trabajador =>
            val doc = new Document()
                .append("trabajador", trabajador.trabajador)
                .append("hmin", trabajador.hmin.getOrElse(0))
                .append("hmax", trabajador.hmax.getOrElse(0))
                .append("dia", trabajador.dia)
                .append("tramo", trabajador.tramo)
                .append("ntienda", trabajador.ntienda)
                .append("nalmacen", trabajador.nalmacen)
            collection.insertOne(doc)
        }
        println("Datos insertados en MongoDB")
        client.close()
    }

    private def listarTrabajadores(): List[Trabajadores] = {
        val datosTresPrimeros = collection.find().limit(3).iterator()
        datosTresPrimeros.toList
       
        val nombres = listaTresPrimeros.map(_.trabajador)
        val horasTienda = listaTresPrimeros.map(_.ntienda.getOrElse(0))
    }

    private def crearGraficaDesdeMongo(): Unit = {
        listarTrabajadores()
        val chart: CategoryChart = new CategoryChartBuilder()
            .width(800).height(600)
            .title("Primeros 3 trabajadores")
            .xAxisTitle("Trabajador")
            .yAxisTitle("Horas")
            .build()

        chart.addSeries("Horas", trabajadores.asJava, horasTienda.map(_.asInstanceOf[Number]).asJava)

    }
}

/* object GraficaSueldos extends App {
  val filename = "sueldos.csv"
  val lines = Source.fromFile(filename).getLines().drop(1).toList

  val ciudades = lines.map(_.split(",")(0))
  val sueldos = lines.map(_.split(",")(1).toInt)

  val chart: CategoryChart = new CategoryChartBuilder()
    .width(1000).height(600)
    .title("Sueldo por Ciudad")
    .xAxisTitle("Ciudad")
    .yAxisTitle("Sueldo")
    .build()

  chart.addSeries("Sueldo", ciudades.asJava, sueldos.map(_.asInstanceOf[Number]).asJava)
  new SwingWrapper(chart).displayChart()
  Thread.sleep(20000)

  // Guardar como SVG
  VectorGraphicsEncoder.saveVectorGraphic(chart, "grafica_sueldos", VectorGraphicsFormat.SVG)

  println("¡Gráfica guardada como grafica_sueldos.pdf!")
} */




