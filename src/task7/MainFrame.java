package task7;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class MainFrame extends JFrame{

    private static final String FRAME_TITLE = "Клиент мгновенных сообщений";
    private static final int FRAME_MINIMUM_WIDTH = 500;
    private static final int FRAME_MINIMUM_HEIGHT = 500;
    private static final int FROM_FIELD_DEFAULT_COLUMNS = 10;
    private static final int TO_FIELD_DEFAULT_COLUMNS = 20;
    private static final int INCOMING_AREA_DEFAULT_ROWS = 10;
    private static final int OUTGOING_AREA_DEFAULT_ROWS = 5;
    private static final int SMALL_GAP = 5;
    private static final int MEDIUM_GAP = 10;
    private static final int LARGE_GAP = 15;
    private static final int SERVER_PORT = 4567;
    private final JTextField textFieldFrom;
    private final JTextField textFieldTo;
    private final JTextArea textAreaIncoming;
    private final JTextArea textAreaOutgoing;
    private boolean cursor=false;
    private boolean ctrl=false;
    private boolean enter=false;
    public MainFrame(){
        super(FRAME_TITLE);
        setMinimumSize(
                new Dimension(FRAME_MINIMUM_WIDTH,FRAME_MINIMUM_HEIGHT));
        // Центрируем окно
        final  Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width -getWidth())/2,(kit.getScreenSize().height - getHeight())/2);

        // Текстовая область для отображения полученных сообщений
        textAreaIncoming = new JTextArea(INCOMING_AREA_DEFAULT_ROWS,0);
        textAreaIncoming.setEditable(false);

        // Контейнер, обеспечивающий прокрутку текстовой области
        final JScrollPane scrollPaneIncoming = new JScrollPane(textAreaIncoming);

        // Подиписи полей
        final JLabel labelFrom = new JLabel("Подпись");
        final JLabel labelTo = new JLabel("Получатель");

        // Поля ввода имени пользователя и IP-адреса получаетлся
        textFieldFrom = new JTextField(FROM_FIELD_DEFAULT_COLUMNS);
        textFieldTo = new JTextField(TO_FIELD_DEFAULT_COLUMNS);

        // Текстовая область для ввода сообщения
        textAreaOutgoing = new JTextArea(OUTGOING_AREA_DEFAULT_ROWS,0);

        // Контейнер обеспечивающий прокрутку текстовой области
        final JScrollPane scrollPaneOutgoing = new JScrollPane(textAreaOutgoing);

        // Панель ввода сообщения
        final JPanel messagePanel = new JPanel();
        messagePanel.setBorder(BorderFactory.createTitledBorder("Сообщение"));

        // Обработка событий крусора
        textAreaOutgoing.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cursor=true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cursor=false;
            }
        });

        // Обработка событий клавиатуры
        textAreaOutgoing.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key =e.getKeyCode();
                if (key ==17) ctrl=true;
                if(key == 10) enter=true;
                if(ctrl && enter && cursor){
                    sendMessage();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int key =e.getKeyCode();
                if (key ==17) ctrl=false;
                if(key == 10) enter=false;
            }
        });

        // Компоновка элементов окна
        final GroupLayout layout1 = new GroupLayout(getContentPane());
        setLayout(layout1);

        layout1.setHorizontalGroup(
                layout1.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                layout1.createParallelGroup()
                                        .addComponent(scrollPaneIncoming)
                                        .addComponent(messagePanel))
                        .addContainerGap());
        layout1.setVerticalGroup(
                layout1.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(scrollPaneIncoming)
                        .addGap(MEDIUM_GAP)
                        .addComponent(messagePanel)
                        .addContainerGap());

        // Кнопка отправки сообщения
        final JButton sendButton = new JButton("Отправить");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        // КОмпоновкаэлементов панели сообщения
        final GroupLayout layout2 = new GroupLayout(messagePanel);
        messagePanel.setLayout(layout2);

        layout2.setHorizontalGroup(
                layout2.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                layout2.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout2.createSequentialGroup()
                                                .addComponent(labelFrom)
                                                .addGap(SMALL_GAP)
                                                .addComponent(textFieldFrom)
                                                .addGap(LARGE_GAP)
                                                .addComponent(labelTo)
                                                .addGap(SMALL_GAP)
                                                .addComponent(textFieldTo))
                                        .addComponent(scrollPaneOutgoing)
                                        .addComponent(sendButton))
                        .addContainerGap());
        layout2.setVerticalGroup(
                layout2.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                layout2.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(labelFrom)
                                        .addGap(SMALL_GAP)
                                        .addComponent(textFieldFrom)
                                        .addGap(LARGE_GAP)
                                        .addComponent(labelTo)
                                        .addGap(SMALL_GAP)
                                        .addComponent(textFieldTo))
                        .addGap(MEDIUM_GAP)
                        .addComponent(scrollPaneOutgoing)
                        .addGap(MEDIUM_GAP)
                        .addComponent(sendButton)
                        .addContainerGap());

        // Создание и запуск потока-обработчика запросов
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    final ServerSocket serverSocket= new ServerSocket(SERVER_PORT);
                    while(!Thread.interrupted()){
                        final Socket socket = serverSocket.accept();
                        final DataInputStream in = new DataInputStream(socket.getInputStream());

                        // Читаем имя отправителя
                        final  String senderName = in.readUTF();

                        // Читаем сообщение
                        final String message = in.readUTF();

                        // Закрываем соеденение
                        socket.close();

                        // Создаём объект даты
                        Date date = new Date();
                        // Выделяем IP-адресс
                        final String address =
                                ((InetSocketAddress) socket.getRemoteSocketAddress())
                                        .getAddress().getHostAddress();
                        // Выводим сообщение в текстовую область
                        textAreaIncoming.append(senderName+" ("+ address+ "): ("+date.toString()+") "+ message + "\n");

                    }
                }catch (IOException e){
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Ошибка в работе сервера","Ошибка",
                            JOptionPane.ERROR_MESSAGE);

                }
            }
        }).start();


    }
    private void sendMessage(){
        try {
            // Получаем неообходимые параметры
            final String senderName = textFieldFrom.getText();
            final String destinationAddress = textFieldTo.getText();
            final String message = textAreaOutgoing.getText();

            // Проверяем, что поля не пустые
            if(senderName.isEmpty()){
                JOptionPane.showMessageDialog(this,
                        "Введите адрес получателя","Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(destinationAddress.isEmpty()){
                JOptionPane.showMessageDialog(this,
                        "Введите адрес узла получателя","Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(message.isEmpty()){
                JOptionPane.showMessageDialog(this,
                        "Введите текст сообщения","Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Создаём сокет для соеденения
            final Socket socket = new Socket(destinationAddress,SERVER_PORT);

            // Открываем поток вывода данных
            final DataOutputStream out =new DataOutputStream(socket.getOutputStream());

            // Записываем в поток имя
            out.writeUTF(senderName);

            // Записываем в поток сообщение
            out.writeUTF(message);

            // Закрываем сокет
            socket.close();

            // Помещаем сообщение в текстовую область вывода
            textAreaIncoming.append("Я ->" + destinationAddress +": " + message + "\n");

            // Очищаем текстовую область ввода сообщения
            textAreaOutgoing.setText("");

        }catch (UnknownHostException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this,
                    "Не удалось отправить сообщение: узел-адресант не найден", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }catch (IOException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this,
                    "Не удалось отправить сообщение","Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}