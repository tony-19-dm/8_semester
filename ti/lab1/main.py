import random
import math
import re
from collections import Counter

def shannon_entropy(probs):
    """Энтропия Шеннона по списку вероятностей."""
    return -sum(p * math.log2(p) for p in probs if p > 0)

def compute_entropies(text):
    """Вычисляет H1, H2, H3 для строки text."""
    n = len(text)
    if n == 0:
        return 0.0, 0.0, 0.0

    # H1
    freq1 = Counter(text)
    probs1 = [freq1[ch] / n for ch in freq1]
    h1 = shannon_entropy(probs1)

    # H2 (пары)
    if n < 2:
        h2 = 0.0
    else:
        pairs = [text[i:i+2] for i in range(n-1)]
        freq2 = Counter(pairs)
        probs2 = [freq2[p] / (n-1) for p in freq2]
        h2 = shannon_entropy(probs2) / 2

    # H3 (тройки)
    if n < 3:
        h3 = 0.0
    else:
        triples = [text[i:i+3] for i in range(n-2)]
        freq3 = Counter(triples)
        probs3 = [freq3[t] / (n-2) for t in freq3]
        h3 = shannon_entropy(probs3) / 3

    return h1, h2, h3

def process_english_text(filename):
    """Читает файл, приводит к нижнему регистру, оставляет только а-я и пробел."""
    with open(filename, 'r', encoding='utf-8') as f:
        text = f.read()
    text = text.lower()
    text = text.replace('ё', 'е')
    text = text.replace('ъ', 'ь')
    text = re.sub(r'[^а-я\s]', '', text)
    text = re.sub(r'[\n]', '', text)
    # with open("test.txt", 'w', encoding='utf-8') as f:
    #     f.write(text)
    return text

def generate_file1(filename, symbols, length):
    with open(filename, 'w', encoding='utf-8') as f:
        for _ in range(length):
            f.write(random.choice(symbols))

def generate_file2(filename, symbols, probs, length):
    with open(filename, 'w', encoding='utf-8') as f:
        for _ in range(length):
            f.write(random.choices(symbols, weights=probs)[0])

def main():
    symbols = ['A', 'B', 'C', 'D']
    probs2 = [0.5, 0.25, 0.15, 0.1]
    length = 20000

    random.seed(42)
    generate_file1("file1.txt", symbols, length)
    generate_file2("file2.txt", symbols, probs2, length)

    files = [
        ("file1.txt", False, symbols, probs2, True),   # имя, нужна ли обработка, алфавит, теор.вер., теор. есть?
        ("file2.txt", False, symbols, probs2, True),
        ("file3.txt", True, None, None, False)
    ]

    results = []
    for fname, need_process, syms, tprobs, has_theory in files:
        if need_process:
            text = process_english_text(fname)
        else:
            with open(fname, 'r', encoding='utf-8') as f:
                text = f.read()

        h1, h2, h3 = compute_entropies(text)

        # Максимальная энтропия (по фактическому набору символов в файле)
        unique_chars = set(text)
        max_ent = math.log2(len(unique_chars)) if unique_chars else 0

        # Теоретическая энтропия
        if has_theory:
            if fname == "file1.txt":
                # равномерное распределение
                p = 1 / len(syms)
                theoretical = shannon_entropy([p] * len(syms))
            else:
                theoretical = shannon_entropy(tprobs)
        else:
            theoretical = None

        results.append((fname, h1, h2, h3, max_ent, theoretical))

    # Вывод таблицы
    print("\n" + "="*80)
    print("Оценки энтропии (в битах на символ)")
    print("="*80)
    print(f"{'Файл':<15} {'H₁':<10} {'H₂':<10} {'H₃':<10} {'Max':<10} {'Теоретическая':<15}")
    print("-"*80)
    for name, h1, h2, h3, mx, th in results:
        th_str = f"{th:.4f}" if th is not None else "---"
        print(f"{name:<15} {h1:<10.4f} {h2:<10.4f} {h3:<10.4f} {mx:<10.4f} {th_str:<15}")

if __name__ == "__main__":
    main()