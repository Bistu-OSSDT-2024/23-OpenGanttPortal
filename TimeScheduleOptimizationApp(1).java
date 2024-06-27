import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
 
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
 
// Event�ඨ���¼����󣬰����¼����ƺ���ֹʱ��
class Event {
   private String name; // �¼�����
   private Date startTime; // ��ʼʱ��
   private Date endTime; // ��ֹʱ��
 
   // Event��Ĺ��캯��
   public Event(String name, Date startTime, Date endTime) {
       this.name = name;
       this.startTime = startTime;
       this.endTime = endTime;
   }
 
   // ����ΪEvent���getter��setter����
   public String getName() {
       return name;
   }
 
   public void setName(String name) {
       this.name = name;
   }
 
   public Date getStartTime() {
       return startTime;
   }
 
   public void setStartTime(Date startTime) {
       this.startTime = startTime;
   }
 
   public Date getEndTime() {
       return endTime;
   }
 
   public void setEndTime(Date endTime) {
       this.endTime = endTime;
   }
 
   // �Ӽ��������¼���Ϣ������Event����
   public static Event inputEvent() {
       Scanner scanner = new Scanner(System.in);
       System.out.print("�������¼�����: ");
       String name = scanner.nextLine();
       System.out.print("�����뿪ʼʱ�� (yyyy-MM-dd HH:mm): ");
       String startTimeStr = scanner.nextLine();
       System.out.print("���������ʱ�� (yyyy-MM-dd HH:mm): ");
       String endTimeStr = scanner.nextLine();
       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
       try {
           Date startTime = dateFormat.parse(startTimeStr);
           Date endTime = dateFormat.parse(endTimeStr);
           return new Event(name, startTime, endTime);
       } catch (ParseException e) {
           System.out.println("���ڽ�������: " + e.getMessage());
           return null;
       }
   }
}
 
// TimeSchedule�����ڴ洢�͹�����ʱ�䰲��
class TimeSchedule {
   private List<Event> events; // ���ڴ洢�¼����б�
 
   // TimeSchedule��Ĺ��캯��
   public TimeSchedule() {
       this.events = new ArrayList<>();
   }
 
   // ���������һ���¼���ʱ�䰲����
   public void addEvent(Event event) {
       events.add(event);
   }
 
   // �������Ƴ�һ���¼�
   public void removeEvent(String name) {
       events.removeIf(event -> event.getName().equals(name));
   }
 
   // ��������ȡ�����¼�
   public List<Event> getEvents() {
       return events;
   }
 
   // ���������������Ҫ��...
   // ...
}
 
// TimeScheduleOptimizationApp�����ڴ���һ�����Ե�������AI�ĳ���
public class TimeScheduleOptimizationApp extends JFrame {
   private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions"; // ����AI��API�˵�
   private static final String API_KEY = "888b9b385be2dea84fc8c6918c0bc466.5Nd6w6EfMklCHe6g"; // ����AI��API��Կ
 
   public TimeScheduleOptimizationApp() {
       super("ʱ�䰲���Ż�Ӧ��"); // ���ô��ڱ���
 
       // ����UI���
       JButton addButton = new JButton("����¼�"); // ����¼���ť
       JButton optimizeButton = new JButton("�Ż�"); // �Ż���ť
       JPanel buttonPanel = new JPanel();
       buttonPanel.add(addButton);
       buttonPanel.add(optimizeButton);
 
       // ���ò���
       setLayout(new BorderLayout());
       add(buttonPanel, BorderLayout.NORTH);
 
       // ��Ӱ�ť�Ķ���
       final TimeSchedule schedule = new TimeSchedule();
       addButton.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               Event event = Event.inputEvent();
               if (event != null) {
                   schedule.addEvent(event);
               }
           }
       });
 
       optimizeButton.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               try {
                   // �Ż�ʱ�䰲��
                   optimizeSchedule(schedule);
                   // ���ɸ���ͼ
                   JFreeChart chart = createGanttChart(schedule);
                   ChartPanel chartPanel = new ChartPanel(chart);
                   add(chartPanel, BorderLayout.CENTER);
                   validate();
               } catch (Exception ex) {
                   JOptionPane.showMessageDialog(TimeScheduleOptimizationApp.this,
                           "�Ż�ʱ�䰲�ų���: " + ex.getMessage(),
                           "����", JOptionPane.ERROR_MESSAGE);
               }
           }
       });
 
       // ���ô���
       setSize(800, 600);
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       setLocationRelativeTo(null);
   }
 
   private void optimizeSchedule(TimeSchedule schedule) throws IOException {
       // ����HttpClientʵ��
       try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
           // ����HttpPostʵ��
           HttpPost httpPost = new HttpPost("https://open.bigmodel.cn/api/paas/v4/chat/completions");
           ObjectMapper mapper = new ObjectMapper();
           // ��������ͷ
           httpPost.setHeader("Authorization", "Bearer " + "888b9b385be2dea84fc8c6918c0bc466.5Nd6w6EfMklCHe6g");
           httpPost.setHeader("Content-Type", "application/json");
           // ��ʱ�䰲��ת��ΪJSON
           String json = mapper.writeValueAsString(schedule.getEvents());
           // ����������
           StringEntity entity = new StringEntity(json);
           httpPost.setEntity(entity);
           // �������󲢻�ȡ��Ӧ
           try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
               if (response.getStatusLine().getStatusCode() != 200) {
                   throw new IOException("�Ż�ʱ�䰲��ʧ��: " + response.getStatusLine().getReasonPhrase());
               }
               // ����Ӧ�ô�����Ӧ�������ȡ���ص�����
               // ���践�ص����Ż�����¼��б�
               // List<Event> optimizedEvents = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<Event>>(){});
               return mapper.readValue(response.getEntity().getContent(), new TypeReference<List<Event>>(){});
               // TODO: ����schedule��events�Է�ӳ�Ż�����¼��б�
               // schedule.setEvents(optimizedEvents);
           }
       } // try-with-resources ���Զ��ر�httpClient
   }
 
   private JFreeChart createGanttChart(TimeSchedule schedule) {
       TaskSeriesCollection dataset = new TaskSeriesCollection();
       TaskSeries series = new TaskSeries("�¼�");
       for (Event event : schedule.getEvents()) {
           series.add(new Task(event.getName(), event.getStartTime(), event.getEndTime()));
       }
       dataset.add(series);
       return ChartFactory.createGanttChart(
               "�Ż����ʱ�䰲��", // ͼ�����
               "�¼�", // domain���ǩ
               "ʱ��", // range���ǩ
               dataset, // ���ݼ�
               true, // �Ƿ���ʾͼ��
               true, // �Ƿ����ɹ�����ʾ
               false // �Ƿ�����URL����
       );
   }
 
   public static void main(String[] args) {
       SwingUtilities.invokeLater(new Runnable() {
           @Override
           public void run() {
               new TimeScheduleOptimizationApp().setVisible(true);
           }
       });
   }
}
