import java.net.URI

rootProject.name = "jdbcoracle"
include("lib")


sourceControl {
    gitRepository(URI.create("https://github.com/sfa-siard/EnterUtilities.git")) {
        producesModule("ch.admin.bar:enterutilities")
    }
    gitRepository(URI.create("https://github.com/sfa-siard/SqlParser.git")) {
        producesModule("ch.admin.bar:SqlParser")
    }
    gitRepository(URI.create("https://github.com/sfa-siard/JdbcBase.git")) {
        producesModule("ch.admin.bar:JdbcBase")
    }
}