package org.nkigen.maps.viewer;
//License: GPL. For details, see Readme.txt file. 

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.nkigen.maps.routing.EQRPoint;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.JMapViewerTree;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.LayerGroup;
import org.openstreetmap.gui.jmapviewer.MapMarkerCircle;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.MapRectangleImpl;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

/**
* Demonstrates the usage of {@link JMapViewer}
*
* @author Jan Peter Stotz
* @author Nelson Kigen(2015)
*
*/
public class EQRViewer extends JFrame implements JMapViewerEventListener  {

 private static final long serialVersionUID = 1L;

 private JMapViewerTree treeMap = null;

 private JLabel zoomLabel=null;
 private JLabel zoomValue=null;

 private JLabel mperpLabelName=null;
 private JLabel mperpLabelValue = null;
 LayerGroup italyGroup;
 Layer trentoLayer;
TrafficWindow traffic_window;

 /**
  * Constructs the {@code Demo}.
  */
 public EQRViewer() {
     super("EQRViewer");
     setSize(400, 400);

     treeMap = new JMapViewerTree("Zones");

     // Listen to the map viewer for user operations so components will
     // receive events and update
     map().addJMVListener(this);

     setLayout(new BorderLayout());
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     setExtendedState(JFrame.MAXIMIZED_BOTH);
     JPanel panel = new JPanel();
     JPanel panelTop = new JPanel();
     JPanel panelBottom = new JPanel();
     JPanel helpPanel = new JPanel();

     mperpLabelName=new JLabel("Meters/Pixels: ");
     mperpLabelValue=new JLabel(String.format("%s",map().getMeterPerPixel()));

     zoomLabel=new JLabel("Zoom: ");
     zoomValue=new JLabel(String.format("%s", map().getZoom()));

     add(panel, BorderLayout.NORTH);
     add(helpPanel, BorderLayout.SOUTH);
     panel.setLayout(new BorderLayout());
     panel.add(panelTop, BorderLayout.NORTH);
     panel.add(panelBottom, BorderLayout.SOUTH);
     JLabel helpLabel = new JLabel("Use right mouse button to move,\n "
             + "left double click or mouse wheel to zoom.");
     helpPanel.add(helpLabel);
     JButton button = new JButton("setDisplayToFitMapMarkers");
     button.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
             map().setDisplayToFitMapMarkers();
         }
     });
     JComboBox<TileSource> tileSourceSelector = new JComboBox<>(new TileSource[] {
             new OsmTileSource.Mapnik(),
             new OsmTileSource.CycleMap(),
             new BingAerialTileSource(),
             new MapQuestOsmTileSource(),
             new MapQuestOpenAerialTileSource() });
     tileSourceSelector.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
             map().setTileSource((TileSource) e.getItem());
         }
     });
     JComboBox<TileLoader> tileLoaderSelector;
     try {
         tileLoaderSelector = new JComboBox<>(new TileLoader[] { new OsmFileCacheTileLoader(map()), new OsmTileLoader(map()) });
     } catch (IOException e) {
         tileLoaderSelector = new JComboBox<>(new TileLoader[] { new OsmTileLoader(map()) });
     }
     tileLoaderSelector.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
             map().setTileLoader((TileLoader) e.getItem());
         }
     });
     map().setTileLoader((TileLoader) tileLoaderSelector.getSelectedItem());
     panelTop.add(tileSourceSelector);
     panelTop.add(tileLoaderSelector);
     final JCheckBox showMapMarker = new JCheckBox("Map markers visible");
     showMapMarker.setSelected(map().getMapMarkersVisible());
     showMapMarker.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
             map().setMapMarkerVisible(showMapMarker.isSelected());
         }
     });
     panelBottom.add(showMapMarker);
     ///
     final JCheckBox showTreeLayers = new JCheckBox("Tree Layers visible");
     showTreeLayers.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
             treeMap.setTreeVisible(showTreeLayers.isSelected());
         }
     });
     panelBottom.add(showTreeLayers);
     ///
     final JCheckBox showToolTip = new JCheckBox("ToolTip visible");
     showToolTip.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
             map().setToolTipText(null);
         }
     });
     panelBottom.add(showToolTip);
     ///
     final JCheckBox showTileGrid = new JCheckBox("Tile grid visible");
     showTileGrid.setSelected(map().isTileGridVisible());
     showTileGrid.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
             map().setTileGridVisible(showTileGrid.isSelected());
         }
     });
     panelBottom.add(showTileGrid);
     final JCheckBox showZoomControls = new JCheckBox("Show zoom controls");
     showZoomControls.setSelected(map().getZoomContolsVisible());
     showZoomControls.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
             map().setZoomContolsVisible(showZoomControls.isSelected());
         }
     });
     panelBottom.add(showZoomControls);
     final JCheckBox scrollWrapEnabled = new JCheckBox("Scrollwrap enabled");
     scrollWrapEnabled.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
             map().setScrollWrapEnabled(scrollWrapEnabled.isSelected());
         }
     });
     panelBottom.add(scrollWrapEnabled);
     panelBottom.add(button);

     panelTop.add(zoomLabel);
     panelTop.add(zoomValue);
     panelTop.add(mperpLabelName);
     panelTop.add(mperpLabelValue);

     add(treeMap, BorderLayout.CENTER);

     //
     italyGroup = new LayerGroup("Italy");
     trentoLayer = italyGroup.addLayer("Trento");
     
     treeMap.addLayer(trentoLayer);

     map().addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
             if (e.getButton() == MouseEvent.BUTTON1) {
                 map().getAttribution().handleAttribution(e.getPoint(), true);
             }
         }
     });

     map().addMouseMotionListener(new MouseAdapter() {
         @Override
         public void mouseMoved(MouseEvent e) {
             Point p = e.getPoint();
             boolean cursorHand = map().getAttribution().handleAttributionCursor(p);
             if (cursorHand) {
                 map().setCursor(new Cursor(Cursor.HAND_CURSOR));
             } else {
                 map().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
             }
             if(showToolTip.isSelected()) map().setToolTipText(map().getPosition(p).toString());
         }
     });
     new ColorWindow();
     traffic_window =    new TrafficWindow();
 }
 
 public TrafficWindow getTrafficWindow(){
	 return traffic_window;
 }
 public  MapMarkerDot addMarker(EQRViewerPoint p){
	// MapMarkerDot mp = new MapMarkerDot(trentoLayer, p.getPoint().getLatitude(), p.getPoint().getLongitude());
	 MapMarkerDot mp = new MapMarkerDot( p.getPoint().getLatitude(), p.getPoint().getLongitude());
	// MapMarkerCircle mp = new MapMarkerCircle(p.getPoint().getLatitude(), p.getPoint().getLongitude(), 5);
	 mp.setColor(p.getColor());
	 mp.setBackColor(p.getColor());
	 map().addMapMarker(mp);
	 return mp;
 }
 public  void removeMarker(MapMarkerDot mp){
	 map().removeMapMarker(mp);
 }

 private JMapViewer map(){
     return treeMap.getViewer();
 }
 private static Coordinate c(double lat, double lon){
     return new Coordinate(lat, lon);
 }

 /**
  * @param args
  */
 /*
 
 public static void main(String[] args) {
      //java.util.Properties systemProperties = System.getProperties();
      //systemProperties.setProperty("http.proxyHost", "localhost");
      //systemProperties.setProperty("http.proxyPort", "8008");
    EQRViewer viewer =  new EQRViewer();
    EQRViewerPoint point = new EQRViewerPoint(new EQRPoint(0.0, 0.0), Color.ORANGE);
    MapMarkerDot dot =  viewer.addMarker(point);
    viewer.setVisible(true);
    viewer.removeMarker(dot);
 }
 
 
*/
 private void updateZoomParameters() {
     if (mperpLabelValue!=null)
         mperpLabelValue.setText(String.format("%s",map().getMeterPerPixel()));
     if (zoomValue!=null)
         zoomValue.setText(String.format("%s", map().getZoom()));
 }

 @Override
 public void processCommand(JMVCommandEvent command) {
     if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) ||
             command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) {
         updateZoomParameters();
     }
 }

}
