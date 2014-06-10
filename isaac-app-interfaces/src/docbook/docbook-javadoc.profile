transformation dbdoclet;

section dbdoclet {
  create-appendix = false;
  class-diagram-font-size = 10;
  class-diagram-width = 800;
  create-author-info = false;
  create-class-diagram = true;
  create-deprecated-info = true;
  create-deprecated-list = true;
  create-exception-info = true;
  create-fully-qualified-names = false;
  create-inherited-from-info = true;
  create-synopsis = true;
} 

section DocBook {
  abstract = """ 
This chapter is dynamically generated based off of the javadocs present in the 
isaac-app-interfaces project.  Those interfaces represent the interfaces and HK2
contracts that may be implemented and plugged into the ISAAC framework.
""";
  add-index = false;  
  document-element = "book";
  title = "ISAAC Contract Interfaces";
} 