import numpy as np
from itertools import product

def read_generator_matrix(filename):
    """Читает порождающую матрицу из файла."""
    with open(filename, 'r', encoding='utf-8') as f:
        n, m = map(int, f.readline().split())
        G = []
        for _ in range(n):
            row = list(map(int, f.readline().split()))
            G.append(row)
    return np.array(G), n, m

def generate_all_codewords(G):
    """
    Генерирует все кодовые слова линейного кода.
    Кодовые слова: c = u * G, где u — все возможные информационные векторы.
    """
    n, m = G.shape
    codewords = []
    
    # Все возможные информационные векторы длины n
    for info_bits in product([0, 1], repeat=n):
        info_vec = np.array(info_bits)
        codeword = np.mod(info_vec @ G, 2)  # умножение по модулю 2
        codewords.append(tuple(codeword))
    
    return codewords

def hamming_weight(vector):
    """Вес Хэмминга вектора (количество единиц)."""
    return sum(vector)

def min_hamming_distance(codewords):
    """
    Вычисляет минимальное кодовое расстояние.
    Для линейного кода это минимальный вес ненулевого кодового слова.
    """
    min_weight = float('inf')
    for cw in codewords:
        weight = hamming_weight(cw)
        if weight > 0 and weight < min_weight:
            min_weight = weight
    return min_weight

def analyze_code(filename):
    """Анализирует код по порождающей матрице."""
    G, n, m = read_generator_matrix(filename)
    
    dimension = n
    
    num_codewords = 2 ** dimension
    
    codewords = generate_all_codewords(G)
    
    # Минимальное кодовое расстояние
    d_min = min_hamming_distance(codewords)
    
    return {
        'filename': filename,
        'n': n,                    
        'm': m,                   
        'num_codewords': num_codewords,
        'd_min': d_min
    }

def main():
    print("\n" + "="*80)
    print("Анализ линейных кодов")
    print("="*80)
    print(f"{'Код':<10} {'n (размерность)':<18} {'m (длина кода)':<18} {'Количество кодовых слов':<25} {'Кодовое расстояние d_min':<25}")
    print("-"*96)
    
    results = []
    for i in range(1, 6):
        filename = f"code{i}.txt"
        try:
            result = analyze_code(filename)
            results.append(result)
            print(f"{filename:<10} {result['n']:<18} {result['m']:<18} {result['num_codewords']:<25} {result['d_min']:<25}")
        except FileNotFoundError:
            print(f"{filename:<10} Файл не найден!")
    
    print("="*80)
    
    print("\nПорождающие матрицы:")
    print("-"*80)
    for i in range(1, 6):
        filename = f"code{i}.txt"
        try:
            G, n, m = read_generator_matrix(filename)
            print(f"\n{filename}: код ({m},{n})")
            print("G =")
            for row in G:
                print(" ".join(map(str, row)))
        except FileNotFoundError:
            pass

if __name__ == "__main__":
    main()