import requests


def process_image(filename):
    API_URL = "https://api-inference.huggingface.co/models/Salesforce/blip-image-captioning-large"
    headers = {"Authorization": "Bearer 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"}
    with open(filename, "rb") as f:
        data = f.read()
    response = requests.post(API_URL, headers=headers, data=data)
    output = response.json()
    caption = output[0]['generated_text']
    if caption.find('arafed') != -1:
        caption = caption.replace('arafed', '')
        caption = caption[1:]
        caption = caption.capitalize()
    if caption.find('araffe') != -1:
        caption = caption.replace('araffe', '')
        caption = caption[1:]
        caption = caption.capitalize()

    url = "https://google-translation-unlimited.p.rapidapi.com/translate"

    payload = {
        "texte": caption,
        "to_lang": "ru"
    }
    
    headers = {
        "content-type": "application/x-www-form-urlencoded",
        "X-RapidAPI-Key": "ead4b75fd5msh0fd504543e76c14p10abfcjsn47478f989389",
        "X-RapidAPI-Host": "google-translation-unlimited.p.rapidapi.com"
    }

    response = requests.post(url, data=payload, headers=headers)
    response = response.json()
    translation = response["translation_data"]["translation"]

    return translation


def text_to_speech(text):
    url = "https://voicerss-text-to-speech.p.rapidapi.com/"
    querystring = {
        "key": "0d5095dcbf03456eb1b44aca32761ddc",
        "src": text,
        "hl": "ru-ru",
        "r": "0",
        "c": "mp3",
        "f": "16khz_16bit_stereo"
    }

    headers = {
        "X-RapidAPI-Key": "ead4b75fd5msh0fd504543e76c14p10abfcjsn47478f989389",
        "X-RapidAPI-Host": "voicerss-text-to-speech.p.rapidapi.com"
    }

    response = requests.get(url, headers=headers, params=querystring)

    # if response.status_code == 200:
        # with open("output.mp3", "wb") as f:
            # f.write(response.content)
        # # stop = timeit.default_timer()
        # pygame.mixer.init()
        # pygame.mixer.music.load("output.mp3")

        # # pygame.mixer.music.play()

        # # while pygame.mixer.music.get_busy():
            # # pygame.time.Clock().tick(10)
    # else:
        # print("Failed to fetch audio file:", response.status_code)
        # # stop = timeit.default_timer()

    if response.status_code == 200:
        return response.content
    else:
        raise Exception(f"Failed to fetch audio file: {response.status_code}")



