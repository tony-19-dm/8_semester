from lab1 import shannon_entropy, compute_entropies, process_text, generate_file1, generate_file2
import heapq
import math
import random
import re
from collections import Counter
class HuffmanNode:
    def __init__(self, prob, symbol=None, left=None, right=None):
        self.prob = prob
        self.symbol = symbol
        self.left = left
        self.right = right
    def __lt__(self, other):
        return self.prob < other.prob

def huffman_codes(freq):
    """Алгоритм Хаффмана"""
    heap = [HuffmanNode(prob, sym) for sym, prob in freq.items()]
    heapq.heapify(heap)
    while len(heap) > 1:
        left = heapq.heappop(heap)
        right = heapq.heappop(heap)
        merged = HuffmanNode(left.prob + right.prob, left=left, right=right)
        heapq.heappush(heap, merged)
    root = heap[0]
    codes = {}
    def traverse(node, code):
        if node.symbol is not None:
            codes[node.symbol] = code
        else:
            traverse(node.left, code + '0')
            traverse(node.right, code + '1')
    traverse(root, '')
    return codes

def shannon_fano_codes(freq):
    """Построение кода Шеннона-Фано"""
    items = sorted(freq.items(), key=lambda x: x[1], reverse=True)
    symbols = [sym for sym, _ in items]
    probs = [prob for _, prob in items]
    codes = {}
    def split(parts, code_prefix):
        if len(parts) == 1:
            codes[parts[0][0]] = code_prefix
            return
        total = sum(prob for _, prob in parts)
        acc = 0
        split_idx = 1
        min_diff = total
        for i in range(1, len(parts)):
            acc += parts[i-1][1]
            diff = abs(total - 2*acc)
            if diff < min_diff:
                min_diff = diff
                split_idx = i
        left = parts[:split_idx]
        right = parts[split_idx:]
        split(left, code_prefix + '0')
        split(right, code_prefix + '1')
    split(list(zip(symbols, probs)), '')
    return codes

def encode_text(text, codes):
    """Кодирование текста в битовую строку"""
    return ''.join(codes[ch] for ch in text)

def avg_code_length(freq, codes):
    """Средняя длина кода"""
    total = sum(freq.values())
    return sum(freq[ch] * len(codes[ch]) for ch in freq) / total

def main():
    symbols = ['A', 'B', 'C', 'D']
    probs2 = [0.5, 0.25, 0.15, 0.1]
    length = 20000
    random.seed(42)
    generate_file1("file1.txt", symbols, length)
    generate_file2("file2.txt", symbols, probs2, length)

    files = [
        ("file1.txt", False, None),
        ("file2.txt", False, None),
        ("file3.txt", True, None)
    ]

    results = []

    for fname, is_russian, _ in files:
        if is_russian:
            text = process_text(fname)
        else:
            with open(fname, 'r', encoding='utf-8') as f:
                text = f.read()
        n = len(text)
        freq = Counter(text)
        # Энтропия исходного текста H1 (первого порядка)
        probs = [freq[ch] / n for ch in freq]
        H = shannon_entropy(probs)

        # Для каждого метода кодирования
        for method_name, code_func in [("Хаффман", huffman_codes), ("Шеннон-Фано", shannon_fano_codes)]:
            codes = code_func(freq)
            # Кодирование
            encoded = encode_text(text, codes)
            # Средняя длина кода
            L_avg = avg_code_length(freq, codes)
            # Избыточность
            redundancy = L_avg - H
            # Оценки энтропии закодированного файла (битовой последовательности)
            h1_bits, h2_bits, h3_bits = compute_entropies(encoded)
            results.append((method_name, fname, redundancy, h1_bits, h2_bits, h3_bits))

    print("\n" + "="*100)
    print("Результаты кодирования")
    print("="*100)
    header = f"{'Метод':<15} {'Файл':<15} {'Избыточность':<15} {'H1 бит':<10} {'H2 бит':<10} {'H3 бит':<10}"
    print(header)
    print("-"*100)
    for row in results:
        method, fname, red, h1, h2, h3 = row
        print(f"{method:<15} {fname:<15} {red:<15.4f} {h1:<10.4f} {h2:<10.4f} {h3:<10.4f}")
    print("="*100)

if __name__ == "__main__":
    main()