import axios from 'axios';

const API_BASE_URL = '/api/quiz';

export const quizService = {
  // Get all quizzes
  async getAllQuizzes() {
    try {
      const response = await axios.get(`${API_BASE_URL}/list`);
      return response.data;
    } catch (error) {
      console.error('Error fetching quizzes:', error);
      throw error;
    }
  },

  // Get quiz by document ID
  async getQuizById(documentId) {
    try {
      const response = await axios.get(`${API_BASE_URL}/${documentId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching quiz:', error);
      throw error;
    }
  },

  // Upload new quiz definition
  async uploadQuiz(quizDefinition) {
    try {
      const response = await axios.post(`${API_BASE_URL}/upload`, quizDefinition);
      return response.data;
    } catch (error) {
      console.error('Error uploading quiz:', error);
      throw error;
    }
  },

  // Process quiz response
  async processResponse(responseData) {
    try {
      const response = await axios.post(`${API_BASE_URL}/response`, responseData);
      return response.data;
    } catch (error) {
      console.error('Error processing quiz response:', error);
      throw error;
    }
  },

  // Get specific step of a quiz
  async getQuizStep(quizId, step) {
    try {
      const response = await axios.get(`${API_BASE_URL}/step`, {
        params: { quizId, step }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching quiz step:', error);
      throw error;
    }
  }
};