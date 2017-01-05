package com.felstar.twicsy

import java.io.{File, InputStream}
import java.net.URL
import java.nio.file.StandardCopyOption

import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.collection.parallel.ForkJoinTaskSupport
import java.nio.file.Files

object Downloader extends App{

  val urlclean = "(?i)((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)".r

  val POOLSIZE=4

  val taskSupport=new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(POOLSIZE))

  val HOST="http://twicsy.com"

  val param1=if (args.length<1) throw new IllegalArgumentException("Please enter @username or search") else args(0)

  val (name,searchpath,picid)=if (!param1.startsWith("@")) {
    (param1,s"tag/$param1","search_pics")
  } else {
    (param1.tail, s"u/${param1.tail}", "user_pic")
  }

  val directory=new File(cleanFilename(name))
  directory.mkdir()

  def cleanFilename(st:String): String ={
    val st2= urlclean.replaceAllIn(st,"")
    val st3=st2.replaceAll("[^ a-zA-Z0-9.-]", "-")
    val st4=st3.replaceAll("---","")
    st4.trim()
  }

  def archivePage(page: String): Unit ={

    def downloadImage(url:String, file: File):Boolean={
      val strImageName = url.substring( url.lastIndexOf("/") + 1 )

      println(s"Saving: $url for $HOST/$page")

      var in:InputStream=null
      try {
        val urlImage = new URL(url)
        in = urlImage.openStream()
        Files.copy(in, file.toPath, StandardCopyOption.REPLACE_EXISTING)
        true
      }
      catch{
        case ex: Exception => println(s"$HOST/$page");println(url);println(ex.getMessage);
          false
      }
      finally{
        if (in!=null) in.close()
      }
    }

    println(page)

    val doc = Jsoup.connect(s"$HOST/$page").get()

    val info = doc.select("div[class=columns nine] div.detail_img_info")
    val infohtml=info.html()
    val timestamp=info.select("span a").text

    val main_pic = doc.select("a[id=outbound.main_pic] img")

    val alt=main_pic.attr("alt")
    val filename=cleanFilename(timestamp+" "+alt)

    val src=main_pic.attr("abs:src")

    val html=
      s"""<html>
         |<body>
         |<h2>
         |$timestamp<br/>
         |<br/>
         |$infohtml
         |</h2>
         |<a href="$HOST/$page"><img src="$src"/></a>
         |</body>
         |</html>
      """.stripMargin

    try {
      val infofile = new File(directory, filename + ".html")
      Files.write(infofile.toPath, List(html).asJava)
    }
    catch{
      case ex:Exception=>
        println("*"*40)
        println(page+" "+filename)

        throw ex
    }
    val imagefile=new File(directory, filename+".jpg")
    downloadImage(src, imagefile)
  }

  def downloadIndexPage(index:Int):Boolean={

    println(s"Handling index page: $index")

    val skip=index*80

    val doc = Jsoup.connect(s"$HOST/$searchpath/skip/$skip").get()

    val links = doc.select(s"[id=$picid]")

    if (links.isEmpty){
      println("Empty page")
      return false
    }

    val linkspar=links.asScala.par
    linkspar.tasksupport = taskSupport
    linkspar.foreach(link=>archivePage(link.attr("href")))

    true
  }

  val ti=System.currentTimeMillis()
  Iterator.from(0).takeWhile(downloadIndexPage).toList

  println(System.currentTimeMillis()-ti)
}