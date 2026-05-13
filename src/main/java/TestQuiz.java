public class TestQuiz {
    public static void main(String[] args) {
        dev.eduplay.services.QuizService service = new dev.eduplay.services.QuizService();
        String result = service.generateQuizJson("Il était une fois un petit chat noir nommé Luna qui aimait jouer avec une balle rouge.");
        System.out.println("Result: " + result);
    }
}
