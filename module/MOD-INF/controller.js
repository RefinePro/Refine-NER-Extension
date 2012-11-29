var logger = Packages.org.slf4j.LoggerFactory.getLogger("NER-extension"),
    refineServlet = Packages.com.google.refine.RefineServlet,
    ner = Packages.org.freeyourmetadata.ner,
    services = ner.services,
    commands = ner.commands;

/* Initialize the extension. */
function init() {
  logger.info("Initializing service manager");
  var serviceManager = new services.NERServiceManager();
  serviceManager.addService(new services.Zemanta());
  serviceManager.addService(new services.DummyNER());
  
  logger.info("Initializing commands");
  register("services", new commands.GetServices(serviceManager));
  
  logger.info("Initializing client resources");
  var resourceManager = Packages.com.google.refine.ClientSideResourceManager;
  resourceManager.addPaths(
    "project/scripts",
    module, [
      "scripts/config.js",
      "dialogs/about.js",
      "dialogs/configuration.js",
      "dialogs/extraction.js",
      "scripts/menus.js",
    ]
  );
  resourceManager.addPaths(
    "project/styles",
    module, [
      "styles/main.less",
      "dialogs/about.less",
      "dialogs/configuration.less",
      "dialogs/extraction.less",
    ]
  );
}

function register(path, command) {
  refineServlet.registerCommand(module, path, command);
}
