package otherHomeworksJavaPartThree;

public class HomeworkFour {

    private volatile char currentLetter = 'A';

    public static void main(String[] args) {
        HomeworkFour homeworkFour = new HomeworkFour();
        Thread thread1 = new Thread(homeworkFour::printA);
        Thread thread2 = new Thread(homeworkFour::printB);
        Thread thread3 = new Thread(homeworkFour::printC);
        thread1.start();
        thread2.start();
        thread3.start();
    }

    public synchronized void printA() {

        try {
            for (int i = 0; i < 10; i++) {
                while (currentLetter != 'A') {
                    wait();
                }
                System.out.print("A");
                currentLetter = 'B';
                notifyAll();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public synchronized void printB() {

        try {
            for (int i = 0; i < 10; i++) {
                while (currentLetter != 'B') {
                    wait();
                }
                System.out.print("B");
                currentLetter = 'C';
                notifyAll();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public synchronized void printC() {
        try {
            for (int i = 0; i < 10; i++) {
                while (currentLetter != 'C') {
                    wait();
                }
                System.out.print("C");
                currentLetter = 'A';
                notifyAll();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
    }
}
