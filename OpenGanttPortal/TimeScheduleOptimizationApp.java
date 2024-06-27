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
 
// Event类定义事件对象，包含事件名称和起止时间
class Event {
   private String name; // 事件名称
   private Date startTime; // 起始时间
   private Date endTime; // 终止时间
 
   // Event类的构造函数
   public Event(String name, Date startTime, Date endTime) {
       this.name = name;
       this.startTime = startTime;
       this.endTime = endTime;
   }
 
   // 以下为Event类的getter和setter方法
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
 
   // 从键盘输入事件信息并创建Event对象
   public static Event inputEvent() {
       Scanner scanner = new Scanner(System.in);
       System.out.print("请输入事件名称: ");
       String name = scanner.nextLine();
       System.out.print("请输入开始时间 (yyyy-MM-dd HH:mm): ");
       String startTimeStr = scanner.nextLine();
       System.out.print("请输入结束时间 (yyyy-MM-dd HH:mm): ");
       String endTimeStr = scanner.nextLine();
       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
       try {
           Date startTime = dateFormat.parse(startTimeStr);
           Date endTime = dateFormat.parse(endTimeStr);
           return new Event(name, startTime, endTime);
       } catch (ParseException e) {
           System.out.println("日期解析错误: " + e.getMessage());
           return null;
       }
   }
}
 
// TimeSchedule类用于存储和管理多个时间安排
class TimeSchedule {
   private List<Event> events; // 用于存储事件的列表
 
   // TimeSchedule类的构造函数
   public TimeSchedule() {
       this.events = new ArrayList<>();
   }
 
   // 方法来添加一个事件到时间安排中
   public void addEvent(Event event) {
       events.add(event);
   }
 
   // 方法来移除一个事件
   public void removeEvent(String name) {
       events.removeIf(event -> event.getName().equals(name));
   }
 
   // 方法来获取所有事件
   public List<Event> getEvents() {
       return events;
   }
 
   // 其他方法（如果需要）...
   // ...
}
 
// TimeScheduleOptimizationApp类用于创建一个可以调用智谱AI的程序
public class TimeScheduleOptimizationApp extends JFrame {
   private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions"; // 智谱AI的API端点
   private static final String API_KEY = "888b9b385be2dea84fc8c6918c0bc466.5Nd6w6EfMklCHe6g"; // 智谱AI的API密钥
 
   public TimeScheduleOptimizationApp() {
       super("时间安排优化应用"); // 设置窗口标题
 
       // 创建UI组件
       JButton addButton = new JButton("添加事件"); // 添加事件按钮
       JButton optimizeButton = new JButton("优化"); // 优化按钮
       JPanel buttonPanel = new JPanel();
       buttonPanel.add(addButton);
       buttonPanel.add(optimizeButton);
 
       // 设置布局
       setLayout(new BorderLayout());
       add(buttonPanel, BorderLayout.NORTH);
 
       // 添加按钮的动作
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
                   // 优化时间安排
                   optimizeSchedule(schedule);
                   // 生成甘特图
                   JFreeChart chart = createGanttChart(schedule);
                   ChartPanel chartPanel = new ChartPanel(chart);
                   add(chartPanel, BorderLayout.CENTER);
                   validate();
               } catch (Exception ex) {
                   JOptionPane.showMessageDialog(TimeScheduleOptimizationApp.this,
                           "优化时间安排出错: " + ex.getMessage(),
                           "错误", JOptionPane.ERROR_MESSAGE);
               }
           }
       });
 
       // 设置窗口
       setSize(800, 600);
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       setLocationRelativeTo(null);
   }
 
   private void optimizeSchedule(TimeSchedule schedule) throws IOException {
       // 创建HttpClient实例
       try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
           // 创建HttpPost实例
           HttpPost httpPost = new HttpPost("https://open.bigmodel.cn/api/paas/v4/chat/completions");
           ObjectMapper mapper = new ObjectMapper();
           // 设置请求头
           httpPost.setHeader("Authorization", "Bearer " + "888b9b385be2dea84fc8c6918c0bc466.5Nd6w6EfMklCHe6g");
           httpPost.setHeader("Content-Type", "application/json");
           // 将时间安排转换为JSON
           String json = mapper.writeValueAsString(schedule.getEvents());
           // 设置请求体
           StringEntity entity = new StringEntity(json);
           httpPost.setEntity(entity);
           // 发送请求并获取响应
           try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
               if (response.getStatusLine().getStatusCode() != 200) {
                   throw new IOException("优化时间安排失败: " + response.getStatusLine().getReasonPhrase());
               }
               // 这里应该处理响应，例如读取返回的数据
               // 假设返回的是优化后的事件列表
               // List<Event> optimizedEvents = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<Event>>(){});
               return mapper.readValue(response.getEntity().getContent(), new TypeReference<List<Event>>(){});
               // TODO: 更新schedule的events以反映优化后的事件列表
               // schedule.setEvents(optimizedEvents);
           }
       } // try-with-resources 会自动关闭httpClient
   }
 
   private JFreeChart createGanttChart(TimeSchedule schedule) {
       TaskSeriesCollection dataset = new TaskSeriesCollection();
       TaskSeries series = new TaskSeries("事件");
       for (Event event : schedule.getEvents()) {
           series.add(new Task(event.getName(), event.getStartTime(), event.getEndTime()));
       }
       dataset.add(series);
       return ChartFactory.createGanttChart(
               "优化后的时间安排", // 图表标题
               "事件", // domain轴标签
               "时间", // range轴标签
               dataset, // 数据集
               true, // 是否显示图例
               true, // 是否生成工具提示
               false // 是否生成URL链接
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
