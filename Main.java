/**
 *
 * @author bagewadi Prajwal.
 * @Date 30.08.2024
 */
package app;
//base
import java.lang.*;
//battery status command
import java.io.BufferedReader;
import java.io.InputStreamReader;
//scheduled code Executor 
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.EventObject;
//Gui
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
//sound Alert
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.LineEvent;
//graph
import org.jfree.chart.*;
//time
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

class Gui extends JFrame implements ChangeListener,ActionListener{
    public JProgressBar jb;
    public JLabel battry,battryper,slide1h,slide2h,s1val,s2val;
    public JSlider charge,discharge;
    //public JOptionPane full,low;
    Font f,f1;
    JButton stop;
    Clip clip;
    AudioInputStream ais;
    public Gui(String appname){
        this.setTitle(appname);
        jb=new JProgressBar();
        jb.setMinimum(0);
        jb.setMaximum(100);
        jb.setStringPainted(true);
        f=new Font("Serif",Font.BOLD,70);
        jb.setBounds(150,190,370,80);
        jb.setFont(f);
        battry=new JLabel("Battery");
        battry.setBounds(150,30,200,270);
        f1=new Font("Serif",Font.BOLD,40);
        battry.setFont(f1);
        battryper=new JLabel();
        battryper.setBounds(150,90,200,300);
        battryper.setFont(f);
        slide1h=new JLabel("Full battery notification:");
        slide1h.setBounds(150,280,300,100);
       
        charge=new JSlider(JSlider.HORIZONTAL,0,100,80);
        charge.setBounds(150,350,400,50);
        charge.setMinorTickSpacing(2);
        charge.setMajorTickSpacing(10);
        charge.setPaintTicks(true);
        charge.setPaintLabels(true);
        charge.addChangeListener(this);
        s1val=new JLabel("Slider Val:"+charge.getValue());
        s1val.setBounds(570,350,100,30);
        
        slide2h=new JLabel("Low battery notification:");
        slide2h.setBounds(150,380,300,100);
       
        discharge=new JSlider(JSlider.HORIZONTAL,0,100,20);
        discharge.setBounds(150,450,400,50);
        discharge.setMinorTickSpacing(2);
        discharge.setMajorTickSpacing(10);
        discharge.setPaintTicks(true);
        discharge.setPaintLabels(true);
        discharge.addChangeListener(this);
        s2val=new JLabel("Slider Val:"+discharge.getValue());
        s2val.setBounds(570,450,100,30);
        
        stop=new JButton("Stop Sound");
        stop.setBounds(150,530,100,30);
        stop.addActionListener(this);
        
        //full=new JOptionPane();
        
        this.add(jb);
        this.add(battry);
        this.add(battryper);
        this.add(slide1h);
        this.add(charge);
        this.add(s1val);
        this.add(slide2h);
        this.add(discharge);
        this.add(s2val);
        this.add(stop);
    }
    
    public void stopSound(){
//        clip.addLineListener(event ->{
//                if(event.getType()==LineEvent.Type.STOP){
//                    clip.close();
//                    try {
//                        ais.close();
//                    }catch (IOException e){
//                        e.printStackTrace();
//                    }
//                }
//            });
        if(clip.isRunning()&&clip!=null){
            System.out.println("Stop sound invoked");
            while(jb.getValue()==(charge.getValue()) || jb.getValue()==(discharge.getValue()))
            clip.stop();
            clip.close();
        }
        if(ais!=null){
            try{
                ais.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    public void playSound(String var){
        //this.stopSound();
        try{
            String fname=var;
            File soundf=new File(fname);
            AudioInputStream ais=AudioSystem.getAudioInputStream(soundf);
            clip=AudioSystem.getClip();
            clip.open(ais);
            clip.start();
//            while(clip.isRunning()){
//                Thread.sleep(100);
//            }
            
            
        }catch(UnsupportedAudioFileException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }catch(LineUnavailableException e){
             e.printStackTrace();
        }//catch(InterruptedException e){
//            e.printStackTrace();
//      }
    }
    
    @Override
    public void stateChanged(ChangeEvent e){
        if(e.getSource()==charge){
            int v1=charge.getValue();
            s1val.setText("val:="+v1);
        }
        if(e.getSource()==discharge){
            int v1=discharge.getValue();
            s2val.setText("val:="+v1);
        }
    }
    @Override
    public void actionPerformed(ActionEvent e){
        if(e.getSource()==stop){
            this.stopSound();
        }
    }
}
public class Main{
    
    public static void main(String[] args){
        //gui class
        Gui f=new Gui("BatteryAlaram");
        f.setSize(800,800);
        f.setLocation(50,50);
        f.setLayout(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
        
        
        //battery code
        ScheduledExecutorService scheduler=Executors.newScheduledThreadPool(1);
        Runnable batteryStat=new Runnable(){
            @Override
            public void run(){
                try{
                    String[] command={"powershell.exe", "-Command", "Get-CimInstance -ClassName Win32_Battery | Select-Object -ExpandProperty EstimatedChargeRemaining"};
                    ProcessBuilder pb=new ProcessBuilder(command);
                    pb.redirectErrorStream(true);
                    Process p=pb.start();
                    InputStreamReader isr=new InputStreamReader(p.getInputStream());
                    BufferedReader br=new BufferedReader(isr);
                    String line;
                    if((line=br.readLine())!=null){
                        f.jb.setValue(Integer.parseInt(line.trim()));
                        System.out.println("Battery:"+line.trim()+"%");
                        
                        //sound
                        Main obj=new Main();
                        if(f.charge.getValue()==Integer.parseInt(line.trim())){
                            f.playSound("C:\\Users\\bagew\\Documents\\NetBeansProjects\\BatteryAppv2\\src\\Alert3.wav");
                            int choice=JOptionPane.showConfirmDialog(null,"Battery Full","Msg",JOptionPane.OK_OPTION);
                            if(choice==JOptionPane.OK_OPTION){
                                f.stopSound();
                            }
                        }
                        else if(f.discharge.getValue()==Integer.parseInt(line.trim())){
                            f.playSound("C:\\Users\\bagew\\Documents\\NetBeansProjects\\BatteryAppv2\\src\\Alert3.wav"); 
                            int choice=JOptionPane.showConfirmDialog(null,"Battery low","Msg",JOptionPane.OK_OPTION);
                            if(choice==JOptionPane.OK_OPTION){
                                f.stopSound();
                            }
                        }
                    }
                    br.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        scheduler.scheduleAtFixedRate(batteryStat,0,5,TimeUnit.SECONDS);
        
        LocalTime val1=LocalTime.now();
        LocalTime val2=LocalTime.now().plusMinutes(60);
        DateTimeFormatter hm=DateTimeFormatter.ofPattern("hh:mm a");
        String time=val1.format(hm);
        String time2=val2.format(hm);
        System.out.println("val="+time);
        System.out.println("val="+time2);
    }
}