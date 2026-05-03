import speech_recognition as sr
import sys

def listen():
    r = sr.Recognizer()
    with sr.Microphone() as source:
        r.adjust_for_ambient_noise(source, duration=0.5)
        # We write to stderr to not pollute stdout which will be caught by Java
        sys.stderr.write("READY\n")
        sys.stderr.flush()
        try:
            audio = r.listen(source, timeout=5, phrase_time_limit=5)
            text = r.recognize_google(audio, language="fr-FR")
            # The result is printed to standard output
            print(text.strip())
            sys.exit(0)
        except sr.WaitTimeoutError:
            print("TIMEOUT")
            sys.exit(1)
        except sr.UnknownValueError:
            print("INCONNU")
            sys.exit(1)
        except Exception as e:
            print("ERREUR")
            sys.exit(1)

if __name__ == "__main__":
    listen()
