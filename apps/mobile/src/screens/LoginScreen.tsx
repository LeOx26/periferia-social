import { ApiError, useLogin } from '@periferia/core'
import { useState } from 'react'
import { ActivityIndicator, Pressable, StyleSheet, Text, TextInput, View } from 'react-native'

export default function LoginScreen() {
  // Otro usuario que la web, para poder ver el tiempo real entre ambos.
  const [username, setUsername] = useState('mafe')
  const [password, setPassword] = useState('Periferia2026!')

  // Mismo hook que usa la web. Cero lógica de autenticación aquí.
  const login = useLogin()

  const error = login.error instanceof ApiError ? login.error : null

  return (
    <View style={styles.screen}>
      <Text style={styles.title}>Periferia Social</Text>
      <Text style={styles.subtitle}>Misma lógica que la web, otra capa de vista.</Text>

      <TextInput
        style={styles.input}
        value={username}
        onChangeText={setUsername}
        placeholder="Usuario"
        placeholderTextColor="#6b7280"
        autoCapitalize="none"
        autoCorrect={false}
      />
      <TextInput
        style={styles.input}
        value={password}
        onChangeText={setPassword}
        placeholder="Contraseña"
        placeholderTextColor="#6b7280"
        secureTextEntry
      />

      {error && <Text style={styles.error}>{error.title}</Text>}

      <Pressable
        style={({ pressed }) => [styles.button, pressed && styles.buttonPressed]}
        onPress={() => login.mutate({ username, password })}
        disabled={login.isPending}
      >
        {login.isPending ? (
          <ActivityIndicator color="#0f1115" />
        ) : (
          <Text style={styles.buttonText}>Entrar</Text>
        )}
      </Pressable>

      <Text style={styles.hint}>leo · mafe · carlos · ana · diego</Text>
    </View>
  )
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    justifyContent: 'center',
    padding: 24,
    backgroundColor: '#0f1115',
    gap: 12,
  },
  title: { color: '#f3f4f6', fontSize: 28, fontWeight: '700' },
  subtitle: { color: '#9ca3af', fontSize: 14, marginBottom: 16 },
  input: {
    backgroundColor: '#181b22',
    borderColor: '#272b35',
    borderWidth: 1,
    borderRadius: 10,
    padding: 14,
    color: '#f3f4f6',
    fontSize: 16,
  },
  error: { color: '#f87171', fontSize: 14 },
  button: {
    backgroundColor: '#7c9cff',
    borderRadius: 10,
    padding: 15,
    alignItems: 'center',
    marginTop: 8,
  },
  buttonPressed: { opacity: 0.8 },
  buttonText: { color: '#0f1115', fontWeight: '700', fontSize: 16 },
  hint: { color: '#6b7280', fontSize: 12, textAlign: 'center', marginTop: 8 },
})
