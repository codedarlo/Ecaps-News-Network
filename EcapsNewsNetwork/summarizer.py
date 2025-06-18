import sys
from sumy.parsers.plaintext import PlaintextParser
from sumy.nlp.tokenizers import Tokenizer
from sumy.summarizers.lex_rank import LexRankSummarizer
from transformers import pipeline

def flash_mode_summarization(text):
    """ Uses Sumy LexRank for extractive summarization. """
    parser = PlaintextParser.from_string(text, Tokenizer("english"))
    summarizer = LexRankSummarizer()
    summary = summarizer(parser.document, 3)  # Extract 3 key sentences

    return " ".join([sentence._text for sentence in summary])

def deep_dive_mode_summarization(text):
    """ Uses a pre-trained BERT model for abstractive summarization. """
    summarizer = pipeline("summarization", model="facebook/bart-large-cnn")
    summary = summarizer(text, max_length=150, min_length=50, do_sample=False)

    return summary[0]['summary_text']

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python3 Summarizer.py <mode> <text>")
        sys.exit(1)

    mode = sys.argv[1]
    text = " ".join(sys.argv[2:])  # Combine all remaining args into text

    if mode == "flash_mode":
        result = flash_mode_summarization(text)
    elif mode == "deep_dive_mode":
        result = deep_dive_mode_summarization(text)
    else:
        result = "Invalid mode selected."

    print(result)  # Java reads this output
