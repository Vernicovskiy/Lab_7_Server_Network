import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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

        // Панель ввода сообщения
        final JPanel messagePanel = new JPanel();
        messagePanel.setBorder(BorderFactory.createTitledBorder("Сообщение"));

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

        // Кнопка отправки сообщения
        final JButton sendButton = new JButton("Отправить");

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
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
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



