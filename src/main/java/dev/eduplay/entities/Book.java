package dev.eduplay.entities;

public class Book {
    private int id;
    private int libraryId;
    private String title;
    private String author;
    private String summary;
    private String coverImage;
    private String pdfFile;
    private String type;
    private int minAge;
    private int maxAge;
    private String language;

    public Book() {}

    public Book(int id, int libraryId, String title, String author, String summary,
                String coverImage, String pdfFile, String type,
                int minAge, int maxAge, String language) {
        this.id = id;
        this.libraryId = libraryId;
        this.title = title;
        this.author = author;
        this.summary = summary;
        this.coverImage = coverImage;
        this.pdfFile = pdfFile;
        this.type = type;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.language = language;
    }

    public Book(int libraryId, String title, String author, String summary,
                String coverImage, String pdfFile, String type,
                int minAge, int maxAge, String language) {
        this.libraryId = libraryId;
        this.title = title;
        this.author = author;
        this.summary = summary;
        this.coverImage = coverImage;
        this.pdfFile = pdfFile;
        this.type = type;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.language = language;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLibraryId() { return libraryId; }
    public void setLibraryId(int libraryId) { this.libraryId = libraryId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getPdfFile() { return pdfFile; }
    public void setPdfFile(String pdfFile) { this.pdfFile = pdfFile; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getMinAge() { return minAge; }
    public void setMinAge(int minAge) { this.minAge = minAge; }

    public int getMaxAge() { return maxAge; }
    public void setMaxAge(int maxAge) { this.maxAge = maxAge; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}