java \
  -Xms1500M \
  -Xmx2500M \
  -Xdock:name="Lego Editor" \
  -Dapple.laf.useScreenMenuBar=true \
  -Dorg.ihtsdo.otf.tcc.query.lucene-root-location=berkeley-db \
  -cp "lib/*" gov.va.isaac.gui.App
