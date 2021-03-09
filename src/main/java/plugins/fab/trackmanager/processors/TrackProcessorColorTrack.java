/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.fab.trackmanager.processors;

import icy.gui.component.ComponentUtil;
import icy.gui.util.GuiUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;

import plugins.fab.trackmanager.PluginTrackManagerProcessor;
import plugins.fab.trackmanager.TrackGroup;
import plugins.fab.trackmanager.TrackSegment;
import plugins.nchenouard.spot.Detection;

/**
 * @author Fabrice de Chaumont
 * 
 */
public class TrackProcessorColorTrack extends PluginTrackManagerProcessor implements ActionListener
{
    Color defaultColor = Color.blue;

    JButton defaultColorButton = new JButton(" ");

    JButton selectionButton;
    JButton enabledButton;
    JButton resetButton;

    String[] processorStrings = {"Random Colors", "Default Color", 
            "Color depends on Track's number. (Use H component of HSL)",
            "Color real detections in blue , virtual detection in orange",
            "Color depends on T. (Use H component of HSL)", "Colors depends on start / end of the track",
            "Color depends on group"};
    JComboBox combo = new JComboBox(processorStrings);
    
    JToggleButton buttonRandomColor = new JToggleButton("Random");
    JToggleButton buttonColorTrackNumber = new JToggleButton("Number");
    JToggleButton buttonRealVirtual = new JToggleButton("Virtual");
    JToggleButton buttonColorT = new JToggleButton("Temporal");
    JToggleButton buttonColorGroup = new JToggleButton("Group");
       

    public TrackProcessorColorTrack()
    {

    	setName("Color Track Processor");
        defaultColorButton.setPreferredSize(new Dimension(100, 20));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        panel.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 5 ) ) );
        		
        panel.add( GuiUtil.createLineBoxPanel( 
        		buttonRandomColor, Box.createHorizontalStrut( 10 ),
        		buttonColorTrackNumber, Box.createHorizontalStrut( 10 ), 
        		buttonRealVirtual , Box.createHorizontalStrut( 10 ),
        		buttonColorT , Box.createHorizontalStrut( 10 ),
        		buttonColorGroup
        		) );
        
        panel.add( GuiUtil.createLineBoxPanel( Box.createVerticalStrut( 5 ) ) );
        
        buttonRandomColor.setToolTipText( "Random Colors" );
        buttonColorTrackNumber.setToolTipText( "Colors depends on Track's number." );
        buttonRealVirtual.setToolTipText( "Colors real detections in blue , virtual detection in orange." );
        buttonColorT.setToolTipText( "Colors depends on T." );
        buttonColorGroup.setToolTipText( "Colors depends on group" );
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add( buttonRandomColor );
        buttonGroup.add( buttonColorTrackNumber );
        buttonGroup.add( buttonRealVirtual );
        buttonGroup.add( buttonColorT );
        buttonGroup.add( buttonColorGroup );
        
        Dimension buttonSize = new Dimension( 70 , 50 );
        ComponentUtil.setFixedSize( buttonRandomColor , buttonSize );
        ComponentUtil.setFixedSize( buttonColorTrackNumber , buttonSize );
        ComponentUtil.setFixedSize( buttonRealVirtual , buttonSize );
        ComponentUtil.setFixedSize( buttonColorT , buttonSize );
        ComponentUtil.setFixedSize( buttonColorGroup , buttonSize );
        
        buttonRandomColor.addActionListener( this );
        buttonColorTrackNumber.addActionListener( this );
        buttonRealVirtual.addActionListener( this );
        buttonColorT.addActionListener( this );
        buttonColorGroup.addActionListener( this );
             
        defaultColorButton.setBackground(defaultColor);
        defaultColorButton.setForeground(defaultColor);
        
        buttonRandomColor.setSelected( true );
    }

    @Override
    public void Compute()
    {

        if (isEnabled())
        {
        	if ( buttonRandomColor.isSelected() )
        		randomColors();

        	if ( buttonColorTrackNumber.isSelected() )
        		colorsDependsOnTrackNumber();

        	if ( buttonRealVirtual.isSelected() )
        		colorsDetectionIfVirtual();

        	if ( buttonColorT.isSelected() )
        		colorsDependsOnT();

        	if ( buttonColorGroup.isSelected() )
        		colorsDependsOnGroup();
        }

    }

    private void colorsDependsOnGroup()
    {

        float factor = 0.8f / trackPool.getTrackGroupList().size();

        for (TrackGroup group : trackPool.getTrackGroupList())
        {
            Color color = Color.getHSBColor(0.2f + (float) trackPool.getTrackGroupList().indexOf(group) * factor, 1, 1);
            for (TrackSegment trackSegment : group.getTrackSegmentList())
            {
                for (Detection detection : trackSegment.getDetectionList())
                {
                    detection.setColor(color);
                }
            }
        }

    }

    boolean randomColorComputed = false;
    ArrayList<Color> colorList = new ArrayList<Color>();

    private void randomColors()
    {

        ArrayList<TrackSegment> trackSegmentList = trackPool.getTrackSegmentList();

        if (colorList.size() != trackSegmentList.size())
            createRandomColors();
        if (!randomColorComputed)
            createRandomColors();

        for (int i = 0; i < trackSegmentList.size(); i++)
        {
            TrackSegment ts = trackSegmentList.get(i);
            for (Detection d : ts.getDetectionList())
                d.setColor(colorList.get(i));
        }

    }

    private void defaultColor()
    {
        for (Detection detection : trackPool.getAllDetection())
        {
            detection.setColor(defaultColor);
        }

    }

    private void createRandomColors()
    {
        colorList = new ArrayList<Color>();
        for (int i = 0; i < trackPool.getTrackSegmentList().size(); i++)
            colorList.add(Color.getHSBColor((float) Math.random(), 1, 1));
        randomColorComputed = true;
    }

    private void colorsDetectionIfVirtual()
    {
        for (Detection d : trackPool.getAllDetection())
        {
            Color color = Color.blue;
            if (d.getDetectionType() == Detection.DETECTIONTYPE_VIRTUAL_DETECTION)
                color = Color.orange;
            d.setColor(color);
        }

    }

    private void colorsDependsOnTrackNumber()
    {
        ArrayList<TrackSegment> trackSegmentList = trackPool.getTrackSegmentList();

        float factor = 0.8f / trackSegmentList.size();

        for (int i = 0; i < trackSegmentList.size(); i++)
        {
            TrackSegment ts = trackSegmentList.get(i);
            for (Detection d : ts.getDetectionList())
            {
                d.setColor(Color.getHSBColor(0.2f + (float) i * factor, 1, 1));
            }
        }
    }

    private void colorsDependsOnTofTrack()
    {
        ArrayList<TrackSegment> trackSegmentList = trackPool.getTrackSegmentList();
        for (TrackSegment ts : trackSegmentList)
        {
            float factor = 0.8f / ts.getDetectionList().size();
            for (int i = 0; i < ts.getDetectionList().size(); i++)
            {
                Detection d = ts.getDetectionList().get(i);
                d.setColor(Color.getHSBColor(0.2f + (float) i * (factor), 1, 1));
            }
        }
    }

    private void colorsDependsOnT()
    {
    	float lastTimePoint = trackPool.getLastDetectionTimePoint();
    	float factor = 0.8f / lastTimePoint;
    	for ( Detection d : trackPool.getAllDetection() )
    	{
    		d.setColor( Color.getHSBColor( 0.2f + (float)d.getT() * factor , 1 , 1 ) );
    	}
    }

    public void actionPerformed(ActionEvent e)
    {
        randomColorComputed = false;
        trackPool.fireTrackEditorProcessorChange();
    }

    @Override
    public void Close()
    {
    }

	@Override
	public void displaySequenceChanged() {

	}
}
