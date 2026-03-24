import math
import random
from collections import Counter
import heapq

def shannon_entropy(probs):
    return -sum(p * math.log2(p) for p in probs if p > 0)

def generate_file2(filename, symbols, probs, length):
    with open(filename, 'w', encoding='utf-8') as f:
        for _ in range(length):
            f.write(random.choices(symbols, weights=probs)[0])

class HuffmanNode:
    def __init__(self, prob, symbol=None, left=None, right=None):
        self.prob = prob
        self.symbol = symbol
        self.left = left
        self.right = right
    def __lt__(self, other):
        return self.prob < other.prob

def huffman_codes(freq):
    """Построение кодов Хаффмана по частотам блоков."""
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

def block_encode(text, block_size, codes):
    """Кодирует текст блоками фиксированной длины."""
    n = len(text)
    encoded = []
    for i in range(0, n - block_size + 1, block_size):
        block = text[i:i+block_size]
        if block in codes:
            encoded.append(codes[block])
    return ''.join(encoded)

def avg_code_length_block(freq, codes, block_size):
    """Средняя длина кода на один символ входной последовательности."""
    total_blocks = sum(freq.values())
    total_bits = sum(freq[block] * len(codes[block]) for block in freq)
    # Возвращаем биты на символ исходного текста
    return total_bits / (total_blocks * block_size)

def get_block_frequencies(text, block_size):
    """Подсчёт частот блоков заданной длины."""
    n = len(text)
    blocks = []
    for i in range(0, n - block_size + 1, block_size):
        blocks.append(text[i:i+block_size])
    return Counter(blocks)

# ---------- Основная программа ----------
def main():
    symbols = ['A', 'B', 'C', 'D']
    probs = [0.5, 0.25, 0.15, 0.1]
    length = 20000
    random.seed(42)
    generate_file2("file2.txt", symbols, probs, length)

    with open("file2.txt", 'r', encoding='utf-8') as f:
        text = f.read()

    H1 = shannon_entropy(probs)

    print("\n" + "="*80)
    print("Блочное кодирование файла file2.txt (неравномерное распределение)")
    print("="*80)
    print(f"Теоретическая энтропия H1 = {H1:.4f} бит/символ")
    print("\nРезультаты:")
    print("-"*80)
    print(f"{'Длина блока n':<15} {'Оценка избыточности на символ':<35}")
    print("-"*80)

    for n in [1, 2, 3, 4]:
        freq = get_block_frequencies(text, n)

        codes = huffman_codes(freq)

        L_avg = avg_code_length_block(freq, codes, n)

        redundancy = L_avg - H1
        
        print(f"{n:<15} {redundancy:<35.6f}")

    print("="*80)

if __name__ == "__main__":
    main()