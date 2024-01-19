PPT Templates
=============

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Coreoz_PPT-Templates&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Coreoz_PPT-Templates)

PPT Template is a small templating library to generate PowerPoint presentations.
This project is mainly based on [POI XSLF](https://poi.apache.org/slideshow/xslf-cookbook.html).

Templates are directly created in PowerPoint using a variable system.
Templates must use the MS Office Open XML format, that means the file extension should be `.pptx`.

Getting started
---------------
Include PPT Templates in your project:
```xml
<dependency>
    <groupId>com.coreoz</groupId>
    <artifactId>ppt-templates</artifactId>
    <version>1.0.1</version>
</dependency>
```

Create a template and fill it with your data:
```java
try(FileOutputStream out = new FileOutputStream("generated.pptx")) {
  new PptMapper()
    .text("title", "Hello")
    .text("subtitle", "World!")
    .processTemplate(PptTemplateDemo.class.getResourceAsStream("/title.pptx"))
    .write(out);
}
```
Tadaaa:

![PPT template hello wold](docs/hello_world.png)

Features
--------
- text replacement,
- image replacement,
- text and shape styling,
- text, shape and image hiding.

This features are directly configurable via the `com.coreoz.ppt.PptMapper` API.

For image replacement, styling or hiding elements, variables must be put on a link
on these elements.

Step 1:

![Link on an image - step 1](docs/change_hyperlink.png)

Step 2:

![Link on an image - step 2](docs/change_hyperlink_popup.png)

Demo
----
See the [demonstration project](demo).
